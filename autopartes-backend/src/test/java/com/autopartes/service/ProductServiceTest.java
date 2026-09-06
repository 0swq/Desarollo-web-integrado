package com.autopartes.service;

import com.autopartes.dto.product.ProductRequest;
import com.autopartes.dto.product.ProductResponse;
import com.autopartes.exception.BusinessException;
import com.autopartes.exception.ResourceNotFoundException;
import com.autopartes.model.Category;
import com.autopartes.model.Product;
import com.autopartes.model.Stock;
import com.autopartes.repository.CategoryRepository;
import com.autopartes.repository.ProductRepository;
import com.autopartes.repository.StockRepository;
import com.autopartes.repository.SupplierRepository;
import com.autopartes.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category testCategory;
    private Product testProduct;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .nombre("Frenos")
                .activo(true)
                .build();

        testProduct = Product.builder()
                .id(10L)
                .sku("PAST-FR-001")
                .nombre("Pastillas de Freno Delanteras")
                .precio(new BigDecimal("120.00"))
                .moneda("PEN")
                .categoria(testCategory)
                .activo(true)
                .createdAt(LocalDateTime.now())
                .build();

        testStock = Stock.builder()
                .id(100L)
                .product(testProduct)
                .cantidad(20)
                .stockMinimo(5)
                .build();
    }

    @Test
    @DisplayName("Debe crear una autoparte exitosamente con su stock inicial")
    void shouldCreateProductSuccessfully() {
        ProductRequest request = new ProductRequest();
        request.setSku("PAST-FR-001");
        request.setNombre("Pastillas de Freno Delanteras");
        request.setPrecio(new BigDecimal("120.00"));
        request.setCategoriaId(1L);
        request.setStockInicial(20);
        request.setStockMinimo(5);

        when(productRepository.existsBySku("PAST-FR-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(stockRepository.findByProductId(testProduct.getId())).thenReturn(Optional.of(testStock));

        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("PAST-FR-001");
        assertThat(response.getStockActual()).isEqualTo(20);
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el código SKU ya existe")
    void shouldThrowExceptionWhenSkuAlreadyExists() {
        ProductRequest request = new ProductRequest();
        request.setSku("PAST-FR-001");
        request.setNombre("Pastillas");

        when(productRepository.existsBySku("PAST-FR-001")).thenReturn(true);

        assertThrows(BusinessException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe obtener producto por ID correctamente")
    void shouldGetProductById() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(stockRepository.findByProductId(10L)).thenReturn(Optional.of(testStock));

        ProductResponse response = productService.getProductById(10L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getNombre()).isEqualTo("Pastillas de Freno Delanteras");
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el producto no existe")
    void shouldThrowNotFoundWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    @DisplayName("Debe realizar soft-delete desactivando la autoparte")
    void shouldSoftDeleteProduct() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));

        productService.deleteProduct(10L);

        assertThat(testProduct.getActivo()).isFalse();
        verify(productRepository).save(testProduct);
    }
}
