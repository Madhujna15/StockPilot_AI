package com.stockpilot.backend.product;

import java.math.BigDecimal;

public final class ProductRules {
    private ProductRules() { }

    public static void validateUnitQuantity(ProductUnit unit, BigDecimal quantity) {
        if (unit == null || quantity == null || quantity.signum() < 0) {
            throw new InvalidProductException("Quantities must be zero or greater.");
        }
        if (!unit.allowsFractionalQuantity() && quantity.stripTrailingZeros().scale() > 0) {
            throw new InvalidProductException("Fractional quantities are allowed only for kg and litre products.");
        }
    }
}
