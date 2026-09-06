package com.autopartes.service;

import com.autopartes.dto.product.ProductRequest;
import com.autopartes.dto.product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductResponse> getAllActiveProducts(Pageable pageable);
    Page<ProductResponse> searchProducts(String query, Pageable pageable);
    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);
    List<ProductResponse> getFeaturedProducts();
    ProductResponse getProductById(Long id);
    ProductResponse getProductBySku(String sku);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}
