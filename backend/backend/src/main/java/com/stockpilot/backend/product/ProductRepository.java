package com.stockpilot.backend.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsByBusiness_IdAndSkuIgnoreCase(UUID businessId, String sku);
    boolean existsByBusiness_IdAndSkuIgnoreCaseAndIdNot(UUID businessId, String sku, UUID productId);
    List<Product> findAllByBusiness_IdAndArchivedFalseOrderByNameAsc(UUID businessId);
    Optional<Product> findByIdAndBusiness_IdAndArchivedFalse(UUID id, UUID businessId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :productId and p.business.id = :businessId and p.archived = false")
    Optional<Product> findActiveForUpdate(@Param("productId") UUID productId,
                                          @Param("businessId") UUID businessId);
}
