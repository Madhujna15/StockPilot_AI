package com.stockpilot.backend.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReceiveInvoiceRequest(
        @NotBlank @Size(max = 80) String reference,
        @NotEmpty @Size(max = 100) List<@Valid Line> lines) {
    public record Line(UUID productId, BigDecimal quantity) { }
}
