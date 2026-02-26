package com.orderops.controller;

import com.orderops.dto.order.CreateOrderRequest;
import com.orderops.dto.order.OrderResponse;
import com.orderops.security.UserPrincipal;
import com.orderops.service.OrderService;
import com.orderops.dto.order.UpdateOrderStatusRequest;
import com.orderops.dto.order.OrderResponse;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @Valid @RequestBody CreateOrderRequest req,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return orderService.create(req, principal);
    }

    @GetMapping
    public Page<OrderResponse> list(Pageable pageable) {
        return orderService.list(pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return orderService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest req
    ) {
        return orderService.updateStatus(id, req);
    }
}
