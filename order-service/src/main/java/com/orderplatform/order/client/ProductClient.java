package com.orderplatform.order.client;

import com.orderplatform.order.client.dto.ProductClientResponse;
import com.orderplatform.order.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${product-service.url}", configuration = FeignClientConfig.class)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductClientResponse getProductById(@PathVariable("id") Long id);
}
