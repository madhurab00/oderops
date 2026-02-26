package com.orderops.dto.order;

import com.orderops.entity.OrderStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class OrderResponse {
    UUID id;
    OrderStatus status;
    Instant createdAt;
    int totalAmountCents;
    UUID createdByUserId;
    List<OrderItemResponse> items;
}