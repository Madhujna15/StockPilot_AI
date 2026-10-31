package com.stockpilot.backend.product;

import com.stockpilot.backend.auth.Role;
import com.stockpilot.backend.business.Business;
import com.stockpilot.backend.product.dto.ProductRequest;
import com.stockpilot.backend.product.dto.ProductResponse;
import com.stockpilot.backend.product.dto.ProductUpdateRequest;
import com.stockpilot.backend.user.AppUser;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository products;
    private final StockMovementRepository movements;

    public ProductService(ProductRepository products, StockMovementRepository movements) {
        this.products = products;
        this.movements = movements;
    }

    @Transactional
    public ProductResponse create(ProductRequest request, AppUser actor) {
        Business business = actor.getBusiness();
        String sku = normalizeSku(request.sku());
        if (products.existsByBusiness_IdAndSkuIgnoreCase(business.getId(), sku)) throw new DuplicateSkuException();
        validateQuantities(request.unit(), request.initialQuantity(), request.reorderLevel(), request.packSize());

        Product product = products.save(new Product(business, request.name().trim(), sku,
                normalizeCategory(request.category()), request.unit(), request.sellingPricePaise(),
                request.costPricePaise(), request.initialQuantity(), request.reorderLevel(),
                request.packSize(), request.leadTimeDays()));
        if (request.initialQuantity().signum() > 0) {
            movements.save(new StockMovement(product, actor, MovementType.OPENING_BALANCE,
                    request.initialQuantity(), "Opening stock"));
        }
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(AppUser actor, String search, String category, ProductStatus status) {
        String query = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        String categoryQuery = category == null ? "" : category.trim().toLowerCase(Locale.ROOT);
        return products.findAllByBusiness_IdAndArchivedFalseOrderByNameAsc(actor.getBusiness().getId()).stream()
                .filter(product -> query.isEmpty() || product.getName().toLowerCase(Locale.ROOT).contains(query)
                        || product.getSku().toLowerCase(Locale.ROOT).contains(query))
                .filter(product -> categoryQuery.isEmpty() || (product.getCategory() != null
                        && product.getCategory().toLowerCase(Locale.ROOT).equals(categoryQuery)))
                .filter(product -> status == null || statusOf(product) == status)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID id, AppUser actor) { return toResponse(requireProduct(id, actor)); }

    @Transactional
    public ProductResponse update(UUID id, ProductUpdateRequest request, AppUser actor) {
        Product product = requireProduct(id, actor);
        String sku = normalizeSku(request.sku());
        if (products.existsByBusiness_IdAndSkuIgnoreCaseAndIdNot(actor.getBusiness().getId(), sku, id)) {
            throw new DuplicateSkuException();
        }
        validateQuantities(request.unit(), product.getCurrentQuantity(), request.reorderLevel(), request.packSize());
        product.updateDetails(request.name().trim(), sku, normalizeCategory(request.category()), request.unit(),
                request.sellingPricePaise(), request.costPricePaise(), request.reorderLevel(),
                request.packSize(), request.leadTimeDays());
        return toResponse(products.save(product));
    }

    @Transactional
    public void archive(UUID id, AppUser actor) { requireProduct(id, actor).archive(); }

    private Product requireProduct(UUID id, AppUser actor) {
        return products.findByIdAndBusiness_IdAndArchivedFalse(id, actor.getBusiness().getId())
                .orElseThrow(ProductNotFoundException::new);
    }

    private void validateQuantities(ProductUnit unit, BigDecimal quantity, BigDecimal reorder, BigDecimal pack) {
        ProductRules.validateUnitQuantity(unit, quantity);
        ProductRules.validateUnitQuantity(unit, reorder);
        ProductRules.validateUnitQuantity(unit, pack);
        if (pack.signum() == 0) throw new InvalidProductException("Pack size must be greater than zero.");
    }

    private String normalizeSku(String sku) { return sku.trim().toUpperCase(Locale.ROOT); }
    private String normalizeCategory(String category) {
        return category == null || category.isBlank() ? null : category.trim();
    }

    private ProductStatus statusOf(Product product) {
        if (product.getCurrentQuantity().signum() == 0) return ProductStatus.OUT;
        if (product.getCurrentQuantity().compareTo(product.getReorderLevel()) <= 0) return ProductStatus.LOW;
        return ProductStatus.IN_STOCK;
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getSku(), product.getCategory(),
                product.getUnit(), product.getSellingPricePaise(), product.getCostPricePaise(),
                product.getCurrentQuantity(), product.getReorderLevel(), product.getPackSize(),
                product.getLeadTimeDays(), statusOf(product), product.getCreatedAt(), product.getUpdatedAt());
    }
}
