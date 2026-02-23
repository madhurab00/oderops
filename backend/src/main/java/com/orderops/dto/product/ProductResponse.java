package com.orderops.dto.product;

import lombok.Builder;
import lombok.Value;
import java.util.UUID;

@Value
@Builder
public class ProductResponse {
    UUID id;
    String sku;
    String name;
    int priceCents;
    int stockQty;
    int lowStockThreshold;
}