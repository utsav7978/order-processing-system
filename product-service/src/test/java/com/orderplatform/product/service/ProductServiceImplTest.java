package com.orderplatform.product.service;

import com.orderplatform.product.dto.ProductRequest;
import com.orderplatform.product.dto.ProductResponse;
import com.orderplatform.product.entity.Product;
import com.orderplatform.product.exception.ResourceNotFoundException;
import com.orderplatform.product.mapper.ProductMapper;
import com.orderplatform.product.repository.ProductRepository;
import com.orderplatform.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        existingProduct = Product.builder()
                .id(1L)
                .name("Mechanical Keyboard")
                .description("Hot-swappable, 75% layout")
                .price(new BigDecimal("89.99"))
                .stockQuantity(25)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getById_returnsProduct_whenFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productMapper.toResponse(existingProduct)).thenReturn(
                ProductResponse.builder().id(1L).name("Mechanical Keyboard").build());

        ProductResponse response = productService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Mechanical Keyboard");
    }

    @Test
    void getById_throwsResourceNotFoundException_whenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesAndReturnsProduct() {
        ProductRequest request = new ProductRequest("Mechanical Keyboard", "Hot-swappable, 75% layout",
                new BigDecimal("89.99"), 25);
        when(productMapper.toEntity(request)).thenReturn(existingProduct);
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);
        when(productMapper.toResponse(existingProduct)).thenReturn(
                ProductResponse.builder().id(1L).name("Mechanical Keyboard").build());

        ProductResponse response = productService.create(request);

        assertThat(response.getName()).isEqualTo("Mechanical Keyboard");
        verify(productRepository).save(existingProduct);
    }

    @Test
    void getAll_returnsMappedList() {
        when(productRepository.findAll()).thenReturn(List.of(existingProduct));
        when(productMapper.toResponse(existingProduct)).thenReturn(
                ProductResponse.builder().id(1L).name("Mechanical Keyboard").build());

        List<ProductResponse> responses = productService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Mechanical Keyboard");
    }

    @Test
    void delete_throwsResourceNotFoundException_whenMissing() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void delete_removesProduct_whenFound() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }
}
