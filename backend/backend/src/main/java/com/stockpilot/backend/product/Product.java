package com.stockpilot.backend.product;

import com.stockpilot.backend.business.Business;
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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products", uniqueConstraints =
        @UniqueConstraint(name = "uk_product_business_sku", columnNames = {"business_id", "sku"}))
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ProductUnit unit;

    @Column(nullable = false)
    private long sellingPricePaise;

    @Column(nullable = false)
    private long costPricePaise;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal currentQuantity;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal reorderLevel;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal packSize;

    @Column(nullable = false)
    private int leadTimeDays;

    @Column(nullable = false)
    private boolean archived;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected Product() { }

    public Product(Business business, String name, String sku, String category, ProductUnit unit,
                   long sellingPricePaise, long costPricePaise, BigDecimal currentQuantity,
                   BigDecimal reorderLevel, BigDecimal packSize, int leadTimeDays) {
        this.business = business;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.unit = unit;
        this.sellingPricePaise = sellingPricePaise;
        this.costPricePaise = costPricePaise;
        this.currentQuantity = currentQuantity;
        this.reorderLevel = reorderLevel;
        this.packSize = packSize;
        this.leadTimeDays = leadTimeDays;
    }

    public void updateDetails(String name, String sku, String category, ProductUnit unit,
                              long sellingPricePaise, long costPricePaise,
                              BigDecimal reorderLevel, BigDecimal packSize, int leadTimeDays) {
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.unit = unit;
        this.sellingPricePaise = sellingPricePaise;
        this.costPricePaise = costPricePaise;
        this.reorderLevel = reorderLevel;
        this.packSize = packSize;
        this.leadTimeDays = leadTimeDays;
        this.updatedAt = Instant.now();
    }

    public void applyStockDelta(BigDecimal delta) {
        BigDecimal next = currentQuantity.add(delta);
        if (next.signum() < 0) throw new InvalidProductException("Stock cannot become negative.");
        this.currentQuantity = next;
        this.updatedAt = Instant.now();
    }

    public void archive() { this.archived = true; this.updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public Business getBusiness() { return business; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public String getCategory() { return category; }
    public ProductUnit getUnit() { return unit; }
    public long getSellingPricePaise() { return sellingPricePaise; }
    public long getCostPricePaise() { return costPricePaise; }
    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public BigDecimal getReorderLevel() { return reorderLevel; }
    public BigDecimal getPackSize() { return packSize; }
    public int getLeadTimeDays() { return leadTimeDays; }
    public boolean isArchived() { return archived; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
