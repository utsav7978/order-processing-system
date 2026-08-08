package com.orderplatform.product.service;

import com.orderplatform.product.dto.ProductRequest;
import com.orderplatform.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse getById(Long id);

    List<ProductResponse> getAll();

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
