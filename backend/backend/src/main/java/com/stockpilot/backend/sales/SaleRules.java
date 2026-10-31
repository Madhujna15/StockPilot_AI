package com.stockpilot.backend.sales;

import com.stockpilot.backend.sales.dto.SaleLineRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class SaleRules {
    private SaleRules() { }

    public static String validateIdempotencyKey(String key) {
        if (key == null || !key.matches("[A-Za-z0-9._:-]{8,64}")) {
            throw new InvalidSaleException("Provide an Idempotency-Key header between 8 and 64 safe characters.");
        }
        return key;
    }

    public static void validateUniqueProducts(List<SaleLineRequest> items) {
        Set<UUID> productIds = new HashSet<>();
        for (SaleLineRequest item : items) {
            if (!productIds.add(item.productId())) {
                throw new InvalidSaleException("Each product can appear only once in a bill; combine its quantities.");
            }
        }
    }
}
