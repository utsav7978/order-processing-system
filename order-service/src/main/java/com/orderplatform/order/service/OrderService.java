package com.orderplatform.order.service;

import com.orderplatform.order.dto.OrderRequest;
import com.orderplatform.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(Long userId, OrderRequest request);

    List<OrderResponse> getOrdersForUser(Long userId);

    OrderResponse getOrderById(Long id, Long userId, boolean isAdmin);

    List<OrderResponse> getRecentOrders(Long userId);
}
