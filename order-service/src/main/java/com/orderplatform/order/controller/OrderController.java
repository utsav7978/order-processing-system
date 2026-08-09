package com.orderplatform.order.controller;

import com.orderplatform.order.dto.OrderRequest;
import com.orderplatform.order.dto.OrderResponse;
import com.orderplatform.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order creation and retrieval. Creating an order publishes an OrderCreatedEvent to Kafka.")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create an order", description = "Verifies each product via Product Service, persists the order, and publishes OrderCreatedEvent")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request,
                                                       Authentication authentication) {
        Long userId = currentUserId(authentication);
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get my orders", description = "Returns all orders placed by the authenticated user")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(orderService.getOrdersForUser(userId));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent orders", description = "Returns the authenticated user's last few orders, served from Redis")
    public ResponseEntity<List<OrderResponse>> getRecentOrders(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(orderService.getRecentOrders(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id", description = "Owners can fetch their own orders; admins can fetch any order")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id, Authentication authentication) {
        Long userId = currentUserId(authentication);
        boolean isAdmin = isAdmin(authentication);
        return ResponseEntity.ok(orderService.getOrderById(id, userId, isAdmin));
    }

    private Long currentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
    }
}
