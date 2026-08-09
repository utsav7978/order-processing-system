package com.orderplatform.order.cache;

import com.orderplatform.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentOrdersCacheService {

    private static final String KEY_PREFIX = "orders:recent:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cache.ttl.recent-orders-minutes:15}")
    private long ttlMinutes;

    @Value("${cache.ttl.recent-orders-size:5}")
    private long maxSize;

    public void addRecentOrder(Long userId, OrderResponse order) {
        String key = KEY_PREFIX + userId;
        ListOperations<String, Object> listOps = redisTemplate.opsForList();

        listOps.leftPush(key, order);
        listOps.trim(key, 0, maxSize - 1);
        redisTemplate.expire(key, Duration.ofMinutes(ttlMinutes));
    }

    @SuppressWarnings("unchecked")
    public List<OrderResponse> getRecentOrders(Long userId) {
        String key = KEY_PREFIX + userId;
        List<Object> raw = redisTemplate.opsForList().range(key, 0, maxSize - 1);
        if (raw == null) {
            return List.of();
        }
        return raw.stream()
                .map(o -> (OrderResponse) o)
                .toList();
    }
}
