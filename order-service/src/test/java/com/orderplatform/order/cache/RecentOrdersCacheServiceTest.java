package com.orderplatform.order.cache;

import com.orderplatform.order.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecentOrdersCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    @InjectMocks
    private RecentOrdersCacheService cacheService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cacheService, "ttlMinutes", 15L);
        ReflectionTestUtils.setField(cacheService, "maxSize", 5L);
    }

    @Test
    void addRecentOrder_doesNotThrow_whenRedisIsDown() {
        when(redisTemplate.opsForList()).thenThrow(new RedisConnectionFailureException("Connection refused"));

        assertThatCode(() -> cacheService.addRecentOrder(5L, OrderResponse.builder().id(1L).build()))
                .doesNotThrowAnyException();
    }

    @Test
    void getRecentOrders_returnsEmptyList_whenRedisIsDown() {
        when(redisTemplate.opsForList()).thenThrow(new RedisConnectionFailureException("Connection refused"));

        List<OrderResponse> result = cacheService.getRecentOrders(5L);

        assertThat(result).isEmpty();
    }

    @Test
    void addRecentOrder_pushesTrimsAndSetsExpiry_whenRedisIsHealthy() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        OrderResponse order = OrderResponse.builder().id(1L).build();

        cacheService.addRecentOrder(5L, order);

        org.mockito.Mockito.verify(listOperations).leftPush("orders:recent:5", order);
        org.mockito.Mockito.verify(listOperations).trim(anyString(), org.mockito.ArgumentMatchers.eq(0L), org.mockito.ArgumentMatchers.eq(4L));
        org.mockito.Mockito.verify(redisTemplate).expire(anyString(), any());
    }
}
