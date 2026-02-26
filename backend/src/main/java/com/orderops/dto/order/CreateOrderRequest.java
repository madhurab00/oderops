package com.orderops.dto.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "order must contain at least one item")
    private List<CreateOrderItemRequest> items;
}