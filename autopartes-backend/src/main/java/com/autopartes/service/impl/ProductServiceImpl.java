package com.autopartes.service.impl;

import com.autopartes.dto.category.CategoryResponse;
import com.autopartes.dto.product.ProductRequest;
import com.autopartes.dto.product.ProductResponse;
import com.autopartes.dto.supplier.SupplierResponse;
import com.autopartes.exception.BusinessException;
import com.autopartes.exception.ResourceNotFoundException;
import com.autopartes.model.*;
import com.autopartes.repository.CategoryRepository;
import com.autopartes.repository.ProductRepository;
import com.autopartes.repository.StockRepository;
import com.autopartes.repository.SupplierRepository;
import com.autopartes.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final StockRepository stockRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllActiveProducts(Pageable pageable) {
        return productRepository.findByActivoTrue(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return getAllActiveProducts(pageable);
        }
        return productRepository.searchActiveProducts(query.trim(), pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoriaIdAndActivoTrue(categoryId, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts() {
        return productRepository.findTop8ByActivoTrueOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "sku", sku));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku().trim())) {
            throw new BusinessException("Ya existe un producto registrado con el SKU: " + request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.getCategoriaId()));

        Supplier supplier = null;
        if (request.getProveedorId() != null) {
            supplier = supplierRepository.findById(request.getProveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", request.getProveedorId()));
        }

        Product product = Product.builder()
                .sku(request.getSku().trim().toUpperCase())
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .precioPromocional(request.getPrecioPromocional())
                .moneda("PEN")
                .imagenUrl(request.getImagenUrl())
                .categoria(category)
                .proveedor(supplier)
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        Product savedProduct = productRepository.save(product);

        // Inicializar stock del producto
        int stockInicial = request.getStockInicial() != null ? request.getStockInicial() : 0;
        int stockMinimo = request.getStockMinimo() != null ? request.getStockMinimo() : 5;

        Stock stock = Stock.builder()
                .product(savedProduct)
                .cantidad(stockInicial)
                .stockMinimo(stockMinimo)
                .ubicacionAlmacen(request.getUbicacionAlmacen())
                .build();

        stockRepository.save(stock);

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        if (!product.getSku().equalsIgnoreCase(request.getSku().trim())
                && productRepository.existsBySku(request.getSku().trim())) {
            throw new BusinessException("Ya existe otro producto con el SKU: " + request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.getCategoriaId()));

        Supplier supplier = null;
        if (request.getProveedorId() != null) {
            supplier = supplierRepository.findById(request.getProveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", request.getProveedorId()));
        }

        product.setSku(request.getSku().trim().toUpperCase());
        product.setNombre(request.getNombre().trim());
        product.setDescripcion(request.getDescripcion());
        product.setPrecio(request.getPrecio());
        product.setPrecioPromocional(request.getPrecioPromocional());
        if (request.getImagenUrl() != null) {
            product.setImagenUrl(request.getImagenUrl());
        }
        product.setCategoria(category);
        product.setProveedor(supplier);
        if (request.getActivo() != null) {
            product.setActivo(request.getActivo());
        }

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        product.setActivo(false);
        productRepository.save(product);
    }

    private ProductResponse mapToResponse(Product product) {
        Stock stock = stockRepository.findByProductId(product.getId()).orElse(null);

        CategoryResponse categoryDTO = CategoryResponse.builder()
                .id(product.getCategoria().getId())
                .nombre(product.getCategoria().getNombre())
                .descripcion(product.getCategoria().getDescripcion())
                .imagenUrl(product.getCategoria().getImagenUrl())
                .activo(product.getCategoria().getActivo())
                .build();

        SupplierResponse supplierDTO = null;
        if (product.getProveedor() != null) {
            supplierDTO = SupplierResponse.builder()
                    .id(product.getProveedor().getId())
                    .ruc(product.getProveedor().getRuc())
                    .razonSocial(product.getProveedor().getRazonSocial())
                    .contactoNombre(product.getProveedor().getContactoNombre())
                    .telefono(product.getProveedor().getTelefono())
                    .email(product.getProveedor().getEmail())
                    .activo(product.getProveedor().getActivo())
                    .build();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .nombre(product.getNombre())
                .descripcion(product.getDescripcion())
                .precio(product.getPrecio())
                .precioPromocional(product.getPrecioPromocional())
                .moneda(product.getMoneda())
                .imagenUrl(product.getImagenUrl())
                .categoria(categoryDTO)
                .proveedor(supplierDTO)
                .stockActual(stock != null ? stock.getCantidad() : 0)
                .stockMinimo(stock != null ? stock.getStockMinimo() : 5)
                .bajoStock(stock != null && stock.isBajoStock())
                .ubicacionAlmacen(stock != null ? stock.getUbicacionAlmacen() : null)
                .activo(product.getActivo())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
