package com.stockpilot.backend.sales.dto;

import com.stockpilot.backend.product.ProductUnit;
import java.math.BigDecimal;
import java.util.UUID;

public record SaleItemResponse(UUID productId, String productName, String sku, ProductUnit unit,
                               BigDecimal quantity, long unitPricePaise, long lineTotalPaise) { }
