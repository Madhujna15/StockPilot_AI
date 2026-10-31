package com.stockpilot.backend.product;

public enum ProductUnit {
    PCS(false), KG(true), LITRE(true), BOX(false);

    private final boolean fractional;
    ProductUnit(boolean fractional) { this.fractional = fractional; }
    public boolean allowsFractionalQuantity() { return fractional; }
}
