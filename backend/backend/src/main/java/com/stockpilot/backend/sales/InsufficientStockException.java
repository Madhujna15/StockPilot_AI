package com.stockpilot.backend.sales;

import java.math.BigDecimal;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName, BigDecimal available, BigDecimal requested) {
        super("Not enough stock for " + productName + ". Available: " + available + ", requested: " + requested + ".");
    }
}
