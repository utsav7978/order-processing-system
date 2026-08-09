package com.orderplatform.order.service;

import com.orderplatform.order.cache.RecentOrdersCacheService;
import com.orderplatform.order.client.ProductClient;
import com.orderplatform.order.client.dto.ProductClientResponse;
import com.orderplatform.order.dto.OrderItemRequest;
import com.orderplatform.order.dto.OrderRequest;
import com.orderplatform.order.dto.OrderResponse;
import com.orderplatform.order.entity.Order;
import com.orderplatform.order.entity.OrderStatus;
import com.orderplatform.order.exception.ProductUnavailableException;
import com.orderplatform.order.kafka.OrderEventProducer;
import com.orderplatform.order.kafka.event.OrderCreatedEvent;
import com.orderplatform.order.mapper.OrderMapper;
import com.orderplatform.order.repository.OrderRepository;
import com.orderplatform.order.service.impl.OrderServiceImpl;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderEventProducer orderEventProducer;

    @Mock
    private RecentOrdersCacheService recentOrdersCacheService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_persistsOrderAndPublishesEvent_whenProductsAvailable() {
        OrderItemRequest itemRequest = new OrderItemRequest(10L, 2);
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        ProductClientResponse product = new ProductClientResponse(10L, "Mechanical Keyboard",
                "desc", new BigDecimal("50.00"), 100);
        when(productClient.getProductById(10L)).thenReturn(product);

        Order savedOrder = Order.builder()
                .id(1L).userId(5L).status(OrderStatus.CREATED)
                .totalAmount(new BigDecimal("100.00")).createdAt(LocalDateTime.now())
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse mappedResponse = OrderResponse.builder().id(1L).userId(5L).build();
        when(orderMapper.toResponse(savedOrder)).thenReturn(mappedResponse);
        when(orderMapper.toOrderCreatedEvent(savedOrder)).thenReturn(
                OrderCreatedEvent.builder().orderId(1L).userId(5L).build());

        OrderResponse response = orderService.createOrder(5L, request);

        assertThat(response.getId()).isEqualTo(1L);
        verify(orderEventProducer).publishOrderCreated(any(OrderCreatedEvent.class));
        verify(recentOrdersCacheService).addRecentOrder(5L, mappedResponse);
    }

    @Test
    void createOrder_throwsProductUnavailableException_whenStockInsufficient() {
        OrderItemRequest itemRequest = new OrderItemRequest(10L, 999);
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        ProductClientResponse product = new ProductClientResponse(10L, "Mechanical Keyboard",
                "desc", new BigDecimal("50.00"), 5);
        when(productClient.getProductById(10L)).thenReturn(product);

        assertThatThrownBy(() -> orderService.createOrder(5L, request))
                .isInstanceOf(ProductUnavailableException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderEventProducer, never()).publishOrderCreated(any());
    }

    @Test
    void createOrder_throwsProductUnavailableException_whenProductDoesNotExist() {
        OrderItemRequest itemRequest = new OrderItemRequest(999L, 1);
        OrderRequest request = new OrderRequest(List.of(itemRequest));

        Request feignRequest = Request.create(Request.HttpMethod.GET, "/api/products/999",
                java.util.Map.of(), null, StandardCharsets.UTF_8, new RequestTemplate());
        when(productClient.getProductById(999L)).thenThrow(
                new FeignException.NotFound("not found", feignRequest, null, null));

        assertThatThrownBy(() -> orderService.createOrder(5L, request))
                .isInstanceOf(ProductUnavailableException.class)
                .hasMessageContaining("does not exist");

        verify(orderRepository, never()).save(any(Order.class));
    }
}
