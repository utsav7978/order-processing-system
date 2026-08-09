package com.orderplatform.notification.cache;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoggingCacheErrorHandlerTest {

    private final LoggingCacheErrorHandler handler = new LoggingCacheErrorHandler();

    @Test
    void handleCacheGetError_doesNotRethrow() {
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("notifications");

        assertThatCode(() -> handler.handleCacheGetError(new RuntimeException("Redis down"), cache, 5L))
                .doesNotThrowAnyException();
    }

    @Test
    void handleCachePutError_doesNotRethrow() {
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("notifications");

        assertThatCode(() -> handler.handleCachePutError(new RuntimeException("Redis down"), cache, 5L, "value"))
                .doesNotThrowAnyException();
    }

    @Test
    void handleCacheEvictError_doesNotRethrow() {
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("notifications");

        assertThatCode(() -> handler.handleCacheEvictError(new RuntimeException("Redis down"), cache, 5L))
                .doesNotThrowAnyException();
    }

    @Test
    void handleCacheClearError_doesNotRethrow() {
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("notifications");

        assertThatCode(() -> handler.handleCacheClearError(new RuntimeException("Redis down"), cache))
                .doesNotThrowAnyException();
    }
}
