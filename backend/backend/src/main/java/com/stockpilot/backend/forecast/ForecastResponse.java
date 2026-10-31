package com.stockpilot.backend.forecast;

import com.stockpilot.backend.product.ProductUnit;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ForecastResponse(
        UUID productId,
        String name,
        String sku,
        ProductUnit unit,
        BigDecimal currentQuantity,
        BigDecimal reorderLevel,
        int leadTimeDays,
        int salesDaysObserved,
        BigDecimal unitsSoldInWindow,
        BigDecimal averageDailySales,
        BigDecimal daysOfCover,
        LocalDate predictedStockOutDate,
        boolean lowStockAlert,
        boolean enoughHistory,
        BigDecimal recommendedQuantity,
        String explanation) { }
