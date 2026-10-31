package com.stockpilot.backend.product.dto;

import com.stockpilot.backend.product.ProductStatus;
import com.stockpilot.backend.product.ProductUnit;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String sku,
        String category,
        ProductUnit unit,
        long sellingPricePaise,
        long costPricePaise,
        BigDecimal currentQuantity,
        BigDecimal reorderLevel,
        BigDecimal packSize,
        int leadTimeDays,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt) { }
