package com.stockpilot.backend.product;

import com.stockpilot.backend.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private MovementType type;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityDelta;

    @Column(nullable = false, length = 200)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected StockMovement() { }

    public StockMovement(Product product, AppUser createdBy, MovementType type,
                         BigDecimal quantityDelta, String reason) {
        this.product = product;
        this.createdBy = createdBy;
        this.type = type;
        this.quantityDelta = quantityDelta;
        this.reason = reason;
    }

    public UUID getId() { return id; }
    public Product getProduct() { return product; }
    public AppUser getCreatedBy() { return createdBy; }
    public MovementType getType() { return type; }
    public BigDecimal getQuantityDelta() { return quantityDelta; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
