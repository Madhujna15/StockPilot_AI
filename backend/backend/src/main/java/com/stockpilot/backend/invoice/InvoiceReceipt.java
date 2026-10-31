package com.stockpilot.backend.invoice;

import com.stockpilot.backend.business.Business;
import com.stockpilot.backend.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoice_receipts", uniqueConstraints =
        @UniqueConstraint(name = "uk_receipt_business_reference", columnNames = {"business_id", "reference"}))
public class InvoiceReceipt {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @Column(nullable = false, length = 80)
    private String reference;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected InvoiceReceipt() { }

    public InvoiceReceipt(Business business, AppUser createdBy, String reference) {
        this.business = business;
        this.createdBy = createdBy;
        this.reference = reference;
    }

    public UUID getId() { return id; }
    public String getReference() { return reference; }
    public Instant getCreatedAt() { return createdAt; }
}
