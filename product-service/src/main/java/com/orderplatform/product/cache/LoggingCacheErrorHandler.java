package com.orderplatform.product.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

/**
 * Without this, Spring's default CacheErrorHandler rethrows any Redis
 * exception (connection refused, timeout, etc.), which would turn a Redis
 * outage into a full outage of every product endpoint - even though the
 * source of truth is MySQL, not the cache. With this handler, a cache
 * failure is logged and treated as a miss/no-op: reads fall through to the
 * DB, writes to the cache are skipped, and the request still succeeds.
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
