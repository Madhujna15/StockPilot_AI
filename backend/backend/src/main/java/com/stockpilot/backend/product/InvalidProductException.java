package com.stockpilot.backend.product;

public class InvalidProductException extends RuntimeException {
    public InvalidProductException(String message) { super(message); }
}
