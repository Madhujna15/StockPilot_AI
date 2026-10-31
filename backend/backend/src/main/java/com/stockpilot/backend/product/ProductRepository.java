package com.stockpilot.backend.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByBusiness_IdAndSkuIgnoreCase(UUID businessId, String sku);
    boolean existsByBusiness_IdAndSkuIgnoreCaseAndIdNot(UUID businessId, String sku, UUID productId);
    List<Product> findAllByBusiness_IdAndArchivedFalseOrderByNameAsc(UUID businessId);
    Optional<Product> findByIdAndBusiness_IdAndArchivedFalse(UUID id, UUID businessId);
}
