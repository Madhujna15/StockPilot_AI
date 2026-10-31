package com.stockpilot.backend.product;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductRulesTest {
    @Test
    void permitsFractionalKilogramsAndLitres() {
        assertDoesNotThrow(() -> ProductRules.validateUnitQuantity(ProductUnit.KG, new BigDecimal("1.250")));
        assertDoesNotThrow(() -> ProductRules.validateUnitQuantity(ProductUnit.LITRE, new BigDecimal("0.5")));
    }

    @Test
    void rejectsFractionalPiecesAndBoxes() {
        assertThrows(InvalidProductException.class,
                () -> ProductRules.validateUnitQuantity(ProductUnit.PCS, new BigDecimal("1.5")));
        assertThrows(InvalidProductException.class,
                () -> ProductRules.validateUnitQuantity(ProductUnit.BOX, new BigDecimal("0.25")));
    }

    @Test
    void rejectsNegativeStock() {
        assertThrows(InvalidProductException.class,
                () -> ProductRules.validateUnitQuantity(ProductUnit.KG, new BigDecimal("-0.5")));
    }
}
