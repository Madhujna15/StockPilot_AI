package com.stockpilot.backend.sales;

public class SaleNotFoundException extends RuntimeException {
    public SaleNotFoundException() { super("Sale was not found in your business."); }
}
