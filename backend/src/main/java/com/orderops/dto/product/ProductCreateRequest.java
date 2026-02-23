package com.orderops.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductCreateRequest {

    @NotBlank(message = "sku is required")
    private String sku;

    @NotBlank(message = "name is required")
    private String name;

    @Min(value = 0, message = "price must be >= 0")
    private int priceCents;

    @Min(value = 0, message = "stock must be >= 0")
    private int stockQty;

    @Min(value = 0, message = "threshold must be >= 0")
    private int lowStockThreshold;
}