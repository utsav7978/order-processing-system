package com.orderplatform.notification.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

/**
 * See product-service's LoggingCacheErrorHandler for the full rationale -
 * same fix, applied here so a Redis outage degrades notification reads to
 * "always hit MySQL" instead of failing the request.
 */
@Component
@Slf4j
public class LoggingCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Redis GET failed for cache '{}' key '{}' - falling through to the database", cache.getName(), key, exception);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Redis PUT failed for cache '{}' key '{}' - continuing without caching this value", cache.getName(), key, exception);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Redis EVICT failed for cache '{}' key '{}' - a stale entry may remain until it expires", cache.getName(), key, exception);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Redis CLEAR failed for cache '{}'", cache.getName(), exception);
    }
}
