package com.autopartes.service;

import com.autopartes.dto.producto.ProductoRequest;
import com.autopartes.model.Producto;
import com.autopartes.model.ProductoCategoria;
import com.autopartes.model.Proveedor;
import com.autopartes.model.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private final com.autopartes.repository.ProductoRepository productoRepository;
    private final com.autopartes.repository.CategoriaRepository categoriaRepository;
    private final com.autopartes.repository.ProveedorRepository proveedorRepository;
    private final com.autopartes.repository.StockRepository stockRepository;
    private final com.autopartes.repository.ProductoCategoriaRepository productoCategoriaRepository;

    public ProductoService(com.autopartes.repository.ProductoRepository productoRepository,
                           com.autopartes.repository.CategoriaRepository categoriaRepository,
                           com.autopartes.repository.ProveedorRepository proveedorRepository,
                           com.autopartes.repository.StockRepository stockRepository,
                           com.autopartes.repository.ProductoCategoriaRepository productoCategoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.proveedorRepository = proveedorRepository;
        this.stockRepository = stockRepository;
        this.productoCategoriaRepository = productoCategoriaRepository;
    }

    public Producto crear(ProductoRequest request) {
        if (productoRepository.existePorSku(request.getSku())) {
            throw new com.autopartes.exception.BusinessException("Ya existe un producto con el SKU: " + request.getSku());
        }

        categoriaRepository.buscarPorId(request.getCategoriaId())
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Categoría no encontrada"));

        Proveedor proveedor = null;
        if (request.getProveedorId() != null) {
            proveedor = proveedorRepository.buscarPorId(request.getProveedorId())
                    .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Proveedor no encontrado"));
        }

        Producto producto = new Producto();
        producto.setId(UUID.randomUUID());
        producto.setSku(request.getSku().trim().toUpperCase());
        producto.setNombre(request.getNombre().trim());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setPrecioPromocional(request.getPrecioPromocional());
        producto.setMoneda("PEN");
        producto.setImagenUrl(request.getImagenUrl());
        producto.setProveedorId(proveedor != null ? proveedor.getId() : null);
        producto.setActivo(request.getActivo() != null ? request.getActivo() : true);
        producto.setFechaCreacion(java.time.LocalDateTime.now());
        producto.setFechaActualizacion(java.time.LocalDateTime.now());

        Producto guardado = productoRepository.guardar(producto);

        // Asociar categoría (N:M)
        ProductoCategoria pc = new ProductoCategoria();
        pc.setProductoId(guardado.getId());
        pc.setCategoriaId(request.getCategoriaId());
        productoCategoriaRepository.guardar(pc);

        // Inicializar stock
        int stockInicial = request.getStockInicial() != null ? request.getStockInicial() : 0;
        int stockMinimo = request.getStockMinimo() != null ? request.getStockMinimo() : 5;

        Stock stock = new Stock();
        stock.setId(UUID.randomUUID());
        stock.setProductoId(guardado.getId());
        stock.setCantidad(stockInicial);
        stock.setStockMinimo(stockMinimo);
        stock.setUbicacionAlmacen(request.getUbicacionAlmacen());
        stock.setFechaActualizacion(java.time.LocalDateTime.now());
        stockRepository.guardar(stock);

        return guardado;
    }

    public Optional<Producto> buscarPorId(UUID id) {
        return productoRepository.buscarPorId(id);
    }

    public Optional<Producto> buscarPorSku(String sku) {
        return productoRepository.buscarPorSku(sku);
    }

    public List<Producto> buscarTodos() {
        return productoRepository.buscarTodos();
    }

    public List<Producto> buscarActivos() {
        return productoRepository.buscarActivos();
    }

    public List<Producto> buscarPorProveedor(UUID proveedorId) {
        return productoRepository.buscarPorProveedor(proveedorId);
    }

    public List<Producto> buscarPorNombreContiene(String texto) {
        return productoRepository.buscarPorNombreContiene(texto);
    }

    public Producto actualizar(UUID id, ProductoRequest request) {
        Producto producto = productoRepository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Producto no encontrado"));

        if (!producto.getSku().equalsIgnoreCase(request.getSku().trim()) && productoRepository.existePorSku(request.getSku())) {
            throw new com.autopartes.exception.BusinessException("Ya existe otro producto con el SKU: " + request.getSku());
        }

        categoriaRepository.buscarPorId(request.getCategoriaId())
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Categoría no encontrada"));

        Proveedor proveedor = null;
        if (request.getProveedorId() != null) {
            proveedor = proveedorRepository.buscarPorId(request.getProveedorId())
                    .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Proveedor no encontrado"));
        }

        producto.setSku(request.getSku().trim().toUpperCase());
        producto.setNombre(request.getNombre().trim());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setPrecioPromocional(request.getPrecioPromocional());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setProveedorId(proveedor != null ? proveedor.getId() : null);
        if (request.getActivo() != null) {
            producto.setActivo(request.getActivo());
        }
        producto.setFechaActualizacion(java.time.LocalDateTime.now());

        productoCategoriaRepository.eliminarPorProducto(producto.getId());
        ProductoCategoria pc = new ProductoCategoria();
        pc.setProductoId(producto.getId());
        pc.setCategoriaId(request.getCategoriaId());
        productoCategoriaRepository.guardar(pc);

        return productoRepository.guardar(producto);
    }

    public void eliminar(UUID id) {
        Producto producto = productoRepository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Producto no encontrado"));
        producto.setActivo(false);
        producto.setFechaActualizacion(java.time.LocalDateTime.now());
        productoRepository.guardar(producto);
    }

    public void toggleActivo(UUID id) {
        Producto producto = productoRepository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Producto no encontrado"));
        producto.setActivo(!Boolean.TRUE.equals(producto.getActivo()));
        producto.setFechaActualizacion(java.time.LocalDateTime.now());
        productoRepository.guardar(producto);
    }

    public long contar() {
        return productoRepository.contar();
    }
}