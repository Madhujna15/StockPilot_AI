package com.stockpilot.backend.sales;

import java.util.List;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {
    List<SaleItem> findAllBySale_IdOrderByIdAsc(UUID saleId);

    @Query("select item from SaleItem item join fetch item.sale sale " +
            "where item.product.id = :productId and sale.business.id = :businessId " +
            "and sale.createdAt >= :since")
    List<SaleItem> findRecentSalesForProduct(@Param("productId") UUID productId,
                                             @Param("businessId") UUID businessId,
                                             @Param("since") Instant since);
}
