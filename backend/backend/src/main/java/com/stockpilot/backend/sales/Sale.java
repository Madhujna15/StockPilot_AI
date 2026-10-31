package com.stockpilot.backend.sales;

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
@Table(name = "sales", uniqueConstraints = @UniqueConstraint(
        name = "uk_sale_business_idempotency", columnNames = {"business_id", "idempotency_key"}))
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Column(name = "total_amount_paise", nullable = false)
    private long totalAmountPaise;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Sale() { }

    public Sale(Business business, AppUser createdBy, String idempotencyKey, long totalAmountPaise) {
        this.business = business;
        this.createdBy = createdBy;
        this.idempotencyKey = idempotencyKey;
        this.totalAmountPaise = totalAmountPaise;
    }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public AppUser getCreatedBy() { return createdBy; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public long getTotalAmountPaise() { return totalAmountPaise; }
    public Instant getCreatedAt() { return createdAt; }
}
