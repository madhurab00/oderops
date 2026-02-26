package com.orderops.dto.order;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class OrderItemResponse {
    UUID productId;
    String sku;
    String name;
    int quantity;
    int unitPriceCents;
    int lineTotalCents;
}