package com.stockpilot.backend.invoice;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceReceiptRepository extends JpaRepository<InvoiceReceipt, UUID> {
    boolean existsByBusiness_IdAndReferenceIgnoreCase(UUID businessId, String reference);
}
