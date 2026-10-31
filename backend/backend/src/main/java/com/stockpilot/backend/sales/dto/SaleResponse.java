package com.stockpilot.backend.sales.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SaleResponse(UUID id, String idempotencyKey, Instant createdAt, UUID createdBy,
                           String createdByName, long totalAmountPaise, List<SaleItemResponse> items) { }
