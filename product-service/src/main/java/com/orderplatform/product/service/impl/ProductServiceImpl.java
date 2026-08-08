package com.orderplatform.product.service.impl;

import com.orderplatform.product.cache.RedisConfig;
import com.orderplatform.product.dto.ProductRequest;
import com.orderplatform.product.dto.ProductResponse;
import com.orderplatform.product.entity.Product;
import com.orderplatform.product.exception.ResourceNotFoundException;
import com.orderplatform.product.mapper.ProductMapper;
import com.orderplatform.product.repository.ProductRepository;
import com.orderplatform.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.PRODUCT_LIST_CACHE, allEntries = true)
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Cacheable(value = RedisConfig.PRODUCT_CACHE, key = "#id")
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    @Cacheable(value = RedisConfig.PRODUCT_LIST_CACHE, key = "'all'")
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = RedisConfig.PRODUCT_CACHE, key = "#id"),
            @CacheEvict(value = RedisConfig.PRODUCT_LIST_CACHE, allEntries = true)
    })
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No product found with id: " + id));
        productMapper.updateEntity(product, request);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = RedisConfig.PRODUCT_CACHE, key = "#id"),
            @CacheEvict(value = RedisConfig.PRODUCT_LIST_CACHE, allEntries = true)
    })
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("No product found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
