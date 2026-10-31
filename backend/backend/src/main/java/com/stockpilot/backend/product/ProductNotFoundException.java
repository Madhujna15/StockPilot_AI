package com.stockpilot.backend.product;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() { super("Product was not found in your business."); }
}
