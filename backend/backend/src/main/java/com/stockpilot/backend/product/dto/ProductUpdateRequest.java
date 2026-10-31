package com.stockpilot.backend.product.dto;

import com.stockpilot.backend.product.ProductUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductUpdateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 64) String sku,
        @Size(max = 80) String category,
        @NotNull ProductUnit unit,
        @NotNull @PositiveOrZero Long sellingPricePaise,
        @NotNull @PositiveOrZero Long costPricePaise,
        @NotNull @DecimalMin("0.0") @Digits(integer = 9, fraction = 3) BigDecimal reorderLevel,
        @NotNull @Positive @Digits(integer = 9, fraction = 3) BigDecimal packSize,
        @Min(0) @Max(365) int leadTimeDays) { }
