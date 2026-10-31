package com.stockpilot.backend.sales;

import com.stockpilot.backend.product.Product;
import com.stockpilot.backend.product.ProductUnit;
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
import java.util.UUID;

@Entity
@Table(name = "sale_items")
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 120)
    private String productName;

    @Column(nullable = false, length = 64)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ProductUnit unit;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price_paise", nullable = false)
    private long unitPricePaise;

    @Column(name = "line_total_paise", nullable = false)
    private long lineTotalPaise;

    protected SaleItem() { }

    public SaleItem(Sale sale, Product product, BigDecimal quantity, long unitPricePaise, long lineTotalPaise) {
        this.sale = sale;
        this.product = product;
        this.productName = product.getName();
        this.sku = product.getSku();
        this.unit = product.getUnit();
        this.quantity = quantity;
        this.unitPricePaise = unitPricePaise;
        this.lineTotalPaise = lineTotalPaise;
    }

    public UUID getId() { return id; }
    public Sale getSale() { return sale; }
    public Product getProduct() { return product; }
    public String getProductName() { return productName; }
    public String getSku() { return sku; }
    public ProductUnit getUnit() { return unit; }
    public BigDecimal getQuantity() { return quantity; }
    public long getUnitPricePaise() { return unitPricePaise; }
    public long getLineTotalPaise() { return lineTotalPaise; }
}
