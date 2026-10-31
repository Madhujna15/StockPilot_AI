package com.stockpilot.backend.sales.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record SaleLineRequest(@NotNull UUID productId,
                              @NotNull @Positive @Digits(integer = 9, fraction = 3) BigDecimal quantity) { }
