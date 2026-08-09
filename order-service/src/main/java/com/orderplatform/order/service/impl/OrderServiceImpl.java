package com.orderplatform.order.service.impl;

import com.orderplatform.order.cache.RecentOrdersCacheService;
import com.orderplatform.order.client.ProductClient;
import com.orderplatform.order.client.dto.ProductClientResponse;
import com.orderplatform.order.dto.OrderItemRequest;
import com.orderplatform.order.dto.OrderRequest;
import com.orderplatform.order.dto.OrderResponse;
import com.orderplatform.order.entity.Order;
import com.orderplatform.order.entity.OrderItem;
import com.orderplatform.order.exception.ProductUnavailableException;
import com.orderplatform.order.exception.ResourceNotFoundException;
import com.orderplatform.order.kafka.OrderEventProducer;
import com.orderplatform.order.mapper.OrderMapper;
import com.orderplatform.order.repository.OrderRepository;
import com.orderplatform.order.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;
    private final OrderEventProducer orderEventProducer;
    private final RecentOrdersCacheService recentOrdersCacheService;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {
        Order order = Order.builder().userId(userId).build();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductClientResponse product = fetchProduct(itemRequest.getProductId());

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new ProductUnavailableException(
                        "Insufficient stock for product '" + product.getName() + "'");
            }

            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.addItem(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        OrderResponse response = orderMapper.toResponse(saved);

        orderEventProducer.publishOrderCreated(orderMapper.toOrderCreatedEvent(saved));
        recentOrdersCacheService.addRecentOrder(userId, response);

        return response;
    }

    @Override
    public List<OrderResponse> getOrdersForUser(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderById(Long id, Long userId, boolean isAdmin) {
        Order order = isAdmin
                ? orderRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("No order found with id: " + id))
                : orderRepository.findByIdAndUserId(id, userId)
                        .orElseThrow(() -> new ResourceNotFoundException("No order found with id: " + id));

        return orderMapper.toResponse(order);
    }

    @Override
    public List<OrderResponse> getRecentOrders(Long userId) {
        return recentOrdersCacheService.getRecentOrders(userId);
    }

    private ProductClientResponse fetchProduct(Long productId) {
        try {
            return productClient.getProductById(productId);
        } catch (FeignException.NotFound ex) {
            throw new ProductUnavailableException("Product with id " + productId + " does not exist");
        } catch (FeignException ex) {
            throw new ProductUnavailableException("Product Service is currently unavailable, please try again shortly");
        }
    }
}
