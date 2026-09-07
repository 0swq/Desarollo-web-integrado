package com.autopartes.service;

import com.autopartes.dto.itemcarrito.ItemCarritoRequest;
import com.autopartes.model.Carrito;
import com.autopartes.model.ItemCarrito;
import com.autopartes.model.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ItemCarritoService {

    private final com.autopartes.repository.ItemCarritoRepository itemRepository;
    private final com.autopartes.repository.CarritoRepository carritoRepository;
    private final com.autopartes.repository.ProductoRepository productoRepository;
    private final com.autopartes.repository.StockRepository stockRepository;

    public ItemCarritoService(com.autopartes.repository.ItemCarritoRepository itemRepository,
                               com.autopartes.repository.CarritoRepository carritoRepository,
                               com.autopartes.repository.ProductoRepository productoRepository,
                               com.autopartes.repository.StockRepository stockRepository) {
        this.itemRepository = itemRepository;
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.stockRepository = stockRepository;
    }

    public ItemCarrito agregarItem(UUID carritoId, ItemCarritoRequest request) {
        Carrito carrito = carritoRepository.buscarPorId(carritoId)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Carrito no encontrado"));

        com.autopartes.model.Producto producto = productoRepository.buscarPorId(request.getProductoId())
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Producto no encontrado"));

        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new com.autopartes.exception.BusinessException("El producto no se encuentra disponible");
        }

        // Verificar stock
        Optional<com.autopartes.model.Stock> stockOpt = stockRepository.buscarPorProducto(producto.getId());
        int stockDisponible = stockOpt.map(s -> s.getCantidad()).orElse(0);

        Optional<ItemCarrito> existenteOpt = itemRepository.buscarPorCarritoYProducto(carritoId, producto.getId());
        int cantidadFinal = request.getCantidad();
        if (existenteOpt.isPresent()) {
            cantidadFinal += existenteOpt.get().getCantidad();
        }

        if (stockDisponible < cantidadFinal) {
            throw new com.autopartes.exception.BusinessException(
                    String.format("Stock insuficiente para %s. Disponible: %d, solicitado: %d",
                            producto.getNombre(), stockDisponible, cantidadFinal));
        }

        BigDecimal precioAplicado = (producto.getPrecioPromocional() != null && producto.getPrecioPromocional().compareTo(BigDecimal.ZERO) > 0)
                ? producto.getPrecioPromocional()
                : producto.getPrecio();

        ItemCarrito item;
        if (existenteOpt.isPresent()) {
            item = existenteOpt.get();
            item.setCantidad(cantidadFinal);
            item.setPrecioUnitario(precioAplicado);
        } else {
            item = new ItemCarrito();
            item.setId(UUID.randomUUID());
            item.setCarritoId(carritoId);
            item.setProductoId(producto.getId());
            item.setCantidad(request.getCantidad());
            item.setPrecioUnitario(precioAplicado);
        }

        ItemCarrito guardado = itemRepository.guardar(item);

        carrito.setFechaActualizacion(java.time.LocalDateTime.now());
        carritoRepository.guardar(carrito);

        return guardado;
    }

    public ItemCarrito actualizarCantidad(UUID carritoId, UUID itemId, Integer nuevaCantidad) {
        ItemCarrito item = itemRepository.buscarPorId(itemId)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Item no encontrado"));

        if (!item.getCarritoId().equals(carritoId)) {
            throw new com.autopartes.exception.BusinessException("El item no pertenece a este carrito");
        }

        if (nuevaCantidad < 1) {
            throw new com.autopartes.exception.BusinessException("La cantidad debe ser al menos 1");
        }

        // Verificar stock
        Optional<com.autopartes.model.Stock> stockOpt = stockRepository.buscarPorProducto(item.getProductoId());
        int stockDisponible = stockOpt.map(s -> s.getCantidad()).orElse(0);

        if (stockDisponible < nuevaCantidad) {
            throw new com.autopartes.exception.BusinessException("Stock insuficiente. Disponible: " + stockDisponible);
        }

        item.setCantidad(nuevaCantidad);
        ItemCarrito guardado = itemRepository.guardar(item);

        Carrito carrito = carritoRepository.buscarPorId(carritoId).orElse(null);
        if (carrito != null) {
            carrito.setFechaActualizacion(java.time.LocalDateTime.now());
            carritoRepository.guardar(carrito);
        }

        return guardado;
    }

    public void eliminarItem(UUID carritoId, UUID itemId) {
        ItemCarrito item = itemRepository.buscarPorId(itemId)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Item no encontrado"));

        if (!item.getCarritoId().equals(carritoId)) {
            throw new com.autopartes.exception.BusinessException("El item no pertenece a este carrito");
        }

        itemRepository.eliminar(itemId);

        Carrito carrito = carritoRepository.buscarPorId(carritoId).orElse(null);
        if (carrito != null) {
            carrito.setFechaActualizacion(java.time.LocalDateTime.now());
            carritoRepository.guardar(carrito);
        }
    }

    public List<ItemCarrito> obtenerItems(UUID carritoId) {
        return itemRepository.buscarPorCarrito(carritoId);
    }

    public Optional<ItemCarrito> buscarPorCarritoYProducto(UUID carritoId, UUID productoId) {
        return itemRepository.buscarPorCarritoYProducto(carritoId, productoId);
    }

    public long contar() {
        return itemRepository.contar();
    }
}