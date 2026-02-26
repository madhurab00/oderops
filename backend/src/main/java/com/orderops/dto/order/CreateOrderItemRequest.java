package com.orderops.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateOrderItemRequest {

    @NotNull
    private UUID productId;

    @Min(1)
    private int quantity;
}