package com.stockpilot.backend.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SaleRequest(@NotEmpty @Size(max = 50) List<@Valid SaleLineRequest> items) { }
