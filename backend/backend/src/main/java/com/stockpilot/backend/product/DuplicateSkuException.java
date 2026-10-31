package com.stockpilot.backend.product;

public class DuplicateSkuException extends RuntimeException {
    public DuplicateSkuException() { super("This SKU is already used by a product in your business."); }
}
