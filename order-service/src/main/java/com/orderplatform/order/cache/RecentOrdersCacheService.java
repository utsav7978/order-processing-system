package com.orderplatform.order.cache;

import com.orderplatform.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * This is a manual RedisTemplate integration (not @Cacheable), so it needs
 * its own resilience: order creation must NOT fail just because the recent-
 * orders cache write failed, and GET /api/orders/recent must NOT 500 just
 * because Redis is unreachable - it should degrade to an empty list instead.
 * (Product/Notification Services get the equivalent behavior for free via
 * their LoggingCacheErrorHandler, since they use @Cacheable.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecentOrdersCacheService {

    private static final String KEY_PREFIX = "orders:recent:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cache.ttl.recent-orders-minutes:15}")
    private long ttlMinutes;

    @Value("${cache.ttl.recent-orders-size:5}")
    private long maxSize;

    public void addRecentOrder(Long userId, OrderResponse order) {
        String key = KEY_PREFIX + userId;
        try {
            ListOperations<String, Object> listOps = redisTemplate.opsForList();
            listOps.leftPush(key, order);
            listOps.trim(key, 0, maxSize - 1);
            redisTemplate.expire(key, Duration.ofMinutes(ttlMinutes));
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable - could not cache recent order for user {}. Order was still created successfully.", userId, ex);
        }
    }

    @SuppressWarnings("unchecked")
    public List<OrderResponse> getRecentOrders(Long userId) {
        String key = KEY_PREFIX + userId;
        try {
            List<Object> raw = redisTemplate.opsForList().range(key, 0, maxSize - 1);
            if (raw == null) {
                return List.of();
            }
            return raw.stream()
                    .map(o -> (OrderResponse) o)
                    .toList();
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable - returning an empty recent-orders list for user {}", userId, ex);
            return List.of();
        }
    }
}
