package com.orderops.service;

import com.orderops.dto.order.CreateOrderRequest;
import com.orderops.dto.order.OrderItemResponse;
import com.orderops.dto.order.OrderResponse;
import com.orderops.dto.order.CreateOrderItemRequest;
import com.orderops.dto.order.OrderResponse;
import com.orderops.dto.order.OrderItemResponse;
import com.orderops.dto.order.UpdateOrderStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.orderops.exception.NotFoundException;
import com.orderops.entity.*;
import com.orderops.exception.ConflictException;
import com.orderops.repository.*;
import com.orderops.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse create(CreateOrderRequest req, UserPrincipal principal) {

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ConflictException("User not found"));

        // Collect product IDs
        List<UUID> productIds = req.getItems().stream()
                .map(CreateOrderItemRequest::getProductId)
                .toList();

        //  Lock products
        List<Product> products = productRepository.findByIdIn(productIds);

        if (products.size() != productIds.size()) {
            throw new ConflictException("One or more products not found");
        }

        Map<UUID, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        int total = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = new Order();
        order.setCreatedBy(user);
        order.setStatus(OrderStatus.PENDING);

        for (CreateOrderItemRequest itemReq : req.getItems()) {
            Product p = productMap.get(itemReq.getProductId());

            if (p.getStockQty() < itemReq.getQuantity()) {
                throw new ConflictException("Insufficient stock for SKU: " + p.getSku());
            }

            p.setStockQty(p.getStockQty() - itemReq.getQuantity());

            int lineTotal = p.getPriceCents() * itemReq.getQuantity();
            total += lineTotal;

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(itemReq.getQuantity());
            oi.setUnitPriceCents(p.getPriceCents());
            oi.setLineTotalCents(lineTotal);

            orderItems.add(oi);
        }

        order.setTotalAmountCents(total);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        savedOrder.setItems(orderItems);

        return toResponse(savedOrder);
    }

    public OrderResponse getById(UUID id) {
    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Order not found"));
    return toResponse(order);
}

    @Transactional
    public OrderResponse updateStatus(UUID orderId, UpdateOrderStatusRequest req) {

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found"));

    if (order.getStatus() != OrderStatus.PENDING) {
        throw new ConflictException("Only PENDING orders can be updated");
    }

    if (req.getStatus() == OrderStatus.CONFIRMED) {
        order.setStatus(OrderStatus.CONFIRMED);
    }
    else if (req.getStatus() == OrderStatus.CANCELLED) {
        // rollback stock
        for (OrderItem item : order.getItems()) {
            Product p = item.getProduct();
            p.setStockQty(p.getStockQty() + item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
    }
    else {
        throw new ConflictException("Invalid status transition");
    }

    return toResponse(order);
}

    private OrderResponse toResponse(Order order) {
    return OrderResponse.builder()
            .id(order.getId())
            .status(order.getStatus())
            .createdAt(order.getCreatedAt())
            .totalAmountCents(order.getTotalAmountCents())
            .createdByUserId(order.getCreatedBy().getId())
            .items(order.getItems().stream().map(oi ->
                    OrderItemResponse.builder()
                            .productId(oi.getProduct().getId())
                            .sku(oi.getProduct().getSku())
                            .name(oi.getProduct().getName())
                            .quantity(oi.getQuantity())
                            .unitPriceCents(oi.getUnitPriceCents())
                            .lineTotalCents(oi.getLineTotalCents())
                            .build()
            ).toList())
            .build();
}
    public Page<OrderResponse> list(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
}

}