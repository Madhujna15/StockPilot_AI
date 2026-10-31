package com.stockpilot.backend.sales;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, UUID> {
    Optional<Sale> findByBusiness_IdAndIdempotencyKey(UUID businessId, String idempotencyKey);
    Optional<Sale> findByIdAndBusiness_Id(UUID saleId, UUID businessId);
    List<Sale> findTop50ByBusiness_IdOrderByCreatedAtDesc(UUID businessId);
}
