package com.stockpilot.backend.forecast;

import com.stockpilot.backend.product.Product;
import com.stockpilot.backend.product.ProductNotFoundException;
import com.stockpilot.backend.product.ProductRepository;
import com.stockpilot.backend.sales.SaleItem;
import com.stockpilot.backend.sales.SaleItemRepository;
import com.stockpilot.backend.user.AppUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForecastService {
    private static final int WINDOW_DAYS = 14;
    private static final int MIN_HISTORY_DAYS = 7;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    private final ProductRepository products;
    private final SaleItemRepository saleItems;
    private final int targetCoverDays;

    public ForecastService(ProductRepository products, SaleItemRepository saleItems,
                           @Value("${app.forecast.target-cover-days:4}") int targetCoverDays) {
        this.products = products;
        this.saleItems = saleItems;
        this.targetCoverDays = targetCoverDays;
    }

    @Transactional(readOnly = true)
    public List<ForecastResponse> all(AppUser actor) {
        return products.findAllByBusiness_IdAndArchivedFalseOrderByNameAsc(actor.getBusiness().getId()).stream()
                .map(product -> calculate(product, actor))
                .toList();
    }

    @Transactional(readOnly = true)
    public ForecastResponse get(UUID productId, AppUser actor) {
        Product product = products.findByIdAndBusiness_IdAndArchivedFalse(productId, actor.getBusiness().getId())
                .orElseThrow(ProductNotFoundException::new);
        return calculate(product, actor);
    }

    private ForecastResponse calculate(Product product, AppUser actor) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        Instant windowStart = today.minusDays(WINDOW_DAYS - 1L).atStartOfDay(BUSINESS_ZONE).toInstant();
        List<SaleItem> recent = saleItems.findRecentSalesForProduct(
                product.getId(), actor.getBusiness().getId(), windowStart);
        Set<LocalDate> activeSalesDays = recent.stream()
                .map(item -> item.getSale().getCreatedAt().atZone(BUSINESS_ZONE).toLocalDate())
                .collect(Collectors.toSet());
        BigDecimal unitsSold = recent.stream().map(SaleItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean enoughHistory = activeSalesDays.size() >= MIN_HISTORY_DAYS;

        BigDecimal velocity = enoughHistory
                ? unitsSold.divide(BigDecimal.valueOf(WINDOW_DAYS), 3, RoundingMode.HALF_UP)
                : null;
        BigDecimal daysOfCover = enoughHistory ? ForecastMath.daysOfCover(product.getCurrentQuantity(), velocity) : null;
        LocalDate stockOutDate = daysOfCover == null ? null
                : today.plusDays(daysOfCover.setScale(0, RoundingMode.CEILING).longValue());
        boolean low = product.getCurrentQuantity().compareTo(product.getReorderLevel()) <= 0
                || (daysOfCover != null && daysOfCover.compareTo(BigDecimal.valueOf(product.getLeadTimeDays())) <= 0);
        BigDecimal recommendation = enoughHistory && velocity.signum() > 0
                ? ForecastMath.recommendedQuantity(velocity, product.getLeadTimeDays(), targetCoverDays,
                        product.getCurrentQuantity(), product.getPackSize())
                : null;

        String explanation = explanation(product, activeSalesDays.size(), unitsSold, velocity,
                daysOfCover, recommendation, enoughHistory);
        return new ForecastResponse(product.getId(), product.getName(), product.getSku(), product.getUnit(),
                product.getCurrentQuantity(), product.getReorderLevel(), product.getLeadTimeDays(),
                activeSalesDays.size(), unitsSold, velocity, daysOfCover, stockOutDate, low,
                enoughHistory, recommendation, explanation);
    }

    private String explanation(Product product, int salesDays, BigDecimal unitsSold, BigDecimal velocity,
                               BigDecimal daysOfCover, BigDecimal recommendation, boolean enoughHistory) {
        if (!enoughHistory) {
            return "Not enough history: " + salesDays + " days with sales in the last " + WINDOW_DAYS
                    + " days; at least " + MIN_HISTORY_DAYS + " are needed."
                    + (product.getCurrentQuantity().compareTo(product.getReorderLevel()) <= 0
                        ? " Stock is at or below the reorder level." : "");
        }
        if (velocity.signum() == 0) return "No units sold in the last " + WINDOW_DAYS + " days; no forecast is made.";
        return "Sold " + unitsSold.stripTrailingZeros().toPlainString() + " units in " + WINDOW_DAYS
                + " days (" + velocity.stripTrailingZeros().toPlainString() + "/day). "
                + "Current stock " + product.getCurrentQuantity().stripTrailingZeros().toPlainString()
                + " gives " + daysOfCover.toPlainString() + " days of cover. "
                + "Order quantity covers " + product.getLeadTimeDays() + " lead-time days plus "
                + targetCoverDays + " target days, rounded to pack size "
                + product.getPackSize().stripTrailingZeros().toPlainString() + ": "
                + recommendation.stripTrailingZeros().toPlainString() + " units.";
    }
}
