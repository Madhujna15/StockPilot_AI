package com.stockpilot.backend.sales;

import com.stockpilot.backend.product.MovementType;
import com.stockpilot.backend.product.Product;
import com.stockpilot.backend.product.ProductRepository;
import com.stockpilot.backend.product.ProductRules;
import com.stockpilot.backend.product.StockMovement;
import com.stockpilot.backend.product.StockMovementRepository;
import com.stockpilot.backend.sales.dto.SaleItemResponse;
import com.stockpilot.backend.sales.dto.SaleLineRequest;
import com.stockpilot.backend.sales.dto.SaleRequest;
import com.stockpilot.backend.sales.dto.SaleResponse;
import com.stockpilot.backend.user.AppUser;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleService {
    private final SaleRepository sales;
    private final SaleItemRepository saleItems;
    private final ProductRepository products;
    private final StockMovementRepository movements;

    public SaleService(SaleRepository sales, SaleItemRepository saleItems,
                       ProductRepository products, StockMovementRepository movements) {
        this.sales = sales;
        this.saleItems = saleItems;
        this.products = products;
        this.movements = movements;
    }

    @Transactional
    public SaleResponse create(SaleRequest request, String rawIdempotencyKey, AppUser actor) {
        String idempotencyKey = SaleRules.validateIdempotencyKey(rawIdempotencyKey);
        UUID businessId = actor.getBusiness().getId();
        var existing = sales.findByBusiness_IdAndIdempotencyKey(businessId, idempotencyKey);
        if (existing.isPresent()) return toResponse(existing.get());
        SaleRules.validateUniqueProducts(request.items());

        Map<UUID, Product> lockedProducts = new HashMap<>();
        request.items().stream().map(SaleLineRequest::productId).sorted(Comparator.naturalOrder()).forEach(productId -> {
            Product product = products.findActiveForUpdate(productId, businessId)
                    .orElseThrow(() -> new com.stockpilot.backend.product.ProductNotFoundException());
            lockedProducts.put(productId, product);
        });

        List<PreparedLine> prepared = new ArrayList<>();
        long totalAmountPaise = 0;
        for (SaleLineRequest item : request.items()) {
            Product product = lockedProducts.get(item.productId());
            BigDecimal quantity = item.quantity();
            ProductRules.validateUnitQuantity(product.getUnit(), quantity);
            if (product.getCurrentQuantity().compareTo(quantity) < 0) {
                throw new InsufficientStockException(product.getName(), product.getCurrentQuantity(), quantity);
            }
            long lineTotal = BigDecimal.valueOf(product.getSellingPricePaise())
                    .multiply(quantity).setScale(0, RoundingMode.HALF_UP).longValueExact();
            totalAmountPaise = Math.addExact(totalAmountPaise, lineTotal);
            prepared.add(new PreparedLine(product, quantity, lineTotal));
        }

        Sale sale = sales.save(new Sale(actor.getBusiness(), actor, idempotencyKey, totalAmountPaise));
        List<SaleItem> persistedItems = new ArrayList<>();
        for (PreparedLine line : prepared) {
            line.product().applyStockDelta(line.quantity().negate());
            persistedItems.add(new SaleItem(sale, line.product(), line.quantity(),
                    line.product().getSellingPricePaise(), line.lineTotalPaise()));
            movements.save(new StockMovement(line.product(), actor, MovementType.SALE,
                    line.quantity().negate(), "Sale " + sale.getId()));
        }
        saleItems.saveAll(persistedItems);
        return toResponse(sale, persistedItems);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> recent(AppUser actor) {
        return sales.findTop50ByBusiness_IdOrderByCreatedAtDesc(actor.getBusiness().getId()).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SaleResponse get(UUID saleId, AppUser actor) {
        Sale sale = sales.findByIdAndBusiness_Id(saleId, actor.getBusiness().getId())
                .orElseThrow(SaleNotFoundException::new);
        return toResponse(sale);
    }

    private SaleResponse toResponse(Sale sale) {
        return toResponse(sale, saleItems.findAllBySale_IdOrderByIdAsc(sale.getId()));
    }

    private SaleResponse toResponse(Sale sale, List<SaleItem> items) {
        List<SaleItemResponse> itemResponses = items.stream().map(item -> new SaleItemResponse(
                item.getProduct().getId(), item.getProductName(), item.getSku(), item.getUnit(),
                item.getQuantity(), item.getUnitPricePaise(), item.getLineTotalPaise())).toList();
        return new SaleResponse(sale.getId(), sale.getIdempotencyKey(), sale.getCreatedAt(),
                sale.getCreatedBy().getId(), sale.getCreatedBy().getFullName(),
                sale.getTotalAmountPaise(), itemResponses);
    }

    private record PreparedLine(Product product, BigDecimal quantity, long lineTotalPaise) { }
}
