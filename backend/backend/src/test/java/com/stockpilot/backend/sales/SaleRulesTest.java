package com.stockpilot.backend.sales;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.stockpilot.backend.sales.dto.SaleLineRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SaleRulesTest {
    @Test
    void requiresAReasonableIdempotencyKey() {
        assertDoesNotThrow(() -> SaleRules.validateIdempotencyKey("bill-2026-0001"));
        assertThrows(InvalidSaleException.class, () -> SaleRules.validateIdempotencyKey("short"));
        assertThrows(InvalidSaleException.class, () -> SaleRules.validateIdempotencyKey("bad key with spaces"));
    }

    @Test
    void rejectsDuplicateProductLinesInOneBill() {
        UUID productId = UUID.randomUUID();
        List<SaleLineRequest> duplicateLines = List.of(
                new SaleLineRequest(productId, new BigDecimal("1")),
                new SaleLineRequest(productId, new BigDecimal("2")));

        assertThrows(InvalidSaleException.class, () -> SaleRules.validateUniqueProducts(duplicateLines));
    }

    @Test
    void acceptsDistinctProductsInOneBill() {
        List<SaleLineRequest> items = List.of(
                new SaleLineRequest(UUID.randomUUID(), new BigDecimal("1")),
                new SaleLineRequest(UUID.randomUUID(), new BigDecimal("2")));

        assertDoesNotThrow(() -> SaleRules.validateUniqueProducts(items));
    }
}
