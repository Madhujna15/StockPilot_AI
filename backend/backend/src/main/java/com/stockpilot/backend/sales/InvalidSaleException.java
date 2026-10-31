package com.stockpilot.backend.sales;

public class InvalidSaleException extends RuntimeException {
    public InvalidSaleException(String message) { super(message); }
}
