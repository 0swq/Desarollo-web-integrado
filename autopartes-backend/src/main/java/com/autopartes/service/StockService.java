package com.autopartes.service;

import com.autopartes.dto.stock.StockRequest;
import com.autopartes.model.MovimientoStock;
import com.autopartes.model.Stock;
import com.autopartes.model.TipoMovimiento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class StockService {

    private final com.autopartes.repository.StockRepository stockRepository;
    private final com.autopartes.repository.MovimientoStockRepository movimientoRepository;
    private final com.autopartes.repository.ProductoRepository productoRepository;

    public StockService(com.autopartes.repository.StockRepository stockRepository,
                        com.autopartes.repository.MovimientoStockRepository movimientoRepository,
                        com.autopartes.repository.ProductoRepository productoRepository) {
        this.stockRepository = stockRepository;
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
    }

    public Optional<Stock> buscarPorId(UUID id) {
        return stockRepository.buscarPorId(id);
    }

    public Optional<Stock> buscarPorProducto(UUID productoId) {
        return stockRepository.buscarPorProducto(productoId);
    }

    public List<Stock> buscarTodos() {
        return stockRepository.buscarTodos();
    }

    public List<Stock> buscarBajoStock() {
        return stockRepository.buscarBajoStock();
    }

    public Stock actualizarConfiguracion(UUID productoId, StockRequest request) {
        Stock stock = stockRepository.buscarPorProducto(productoId)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Stock no encontrado para el producto"));

        if (request.getStockMinimo() != null) {
            stock.setStockMinimo(request.getStockMinimo());
        }
        if (request.getUbicacionAlmacen() != null) {
            stock.setUbicacionAlmacen(request.getUbicacionAlmacen());
        }
        stock.setFechaActualizacion(java.time.LocalDateTime.now());

        return stockRepository.guardar(stock);
    }

    public MovimientoStock registrarEntrada(UUID productoId, Integer cantidad, String motivo, String referencia) {
        return registrarMovimiento(productoId, TipoMovimiento.ENTRADA, cantidad, motivo, referencia, null);
    }

    public MovimientoStock registrarSalida(UUID productoId, Integer cantidad, String motivo, String referencia) {
        return registrarMovimiento(productoId, TipoMovimiento.SALIDA, cantidad, motivo, referencia, null);
    }

    public MovimientoStock registrarSalidaAutomatica(UUID productoId, Integer cantidad, String numeroOrden) {
        String motivo = "Venta realizada - Orden " + numeroOrden;
        return registrarMovimiento(productoId, TipoMovimiento.SALIDA, cantidad, motivo, numeroOrden, null);
    }

    public MovimientoStock registrarEntradaAutomatica(UUID productoId, Integer cantidad, String numeroOrden) {
        String motivo = "Cancelación de Orden " + numeroOrden + " - Retorno a inventario";
        return registrarMovimiento(productoId, TipoMovimiento.ENTRADA, cantidad, motivo, numeroOrden, null);
    }

    public MovimientoStock registrarAjuste(UUID productoId, Integer cantidadNueva, String motivo, String referencia, UUID usuarioId) {
        return registrarMovimiento(productoId, TipoMovimiento.AJUSTE, cantidadNueva, motivo, referencia, usuarioId);
    }

    private MovimientoStock registrarMovimiento(UUID productoId, TipoMovimiento tipo, Integer cantidad, String motivo, String referencia, UUID usuarioId) {
        productoRepository.buscarPorId(productoId)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Producto no encontrado"));

        Stock stock = stockRepository.buscarPorProducto(productoId)
                .orElseGet(() -> {
                    Stock nuevo = new Stock();
                    nuevo.setId(UUID.randomUUID());
                    nuevo.setProductoId(productoId);
                    nuevo.setCantidad(0);
                    nuevo.setStockMinimo(5);
                    nuevo.setFechaActualizacion(java.time.LocalDateTime.now());
                    return stockRepository.guardar(nuevo);
                });

        int stockAnterior = stock.getCantidad();
        int stockNuevo;

        switch (tipo) {
            case ENTRADA:
                stockNuevo = stockAnterior + cantidad;
                break;
            case SALIDA:
                if (stockAnterior < cantidad) {
                    throw new com.autopartes.exception.BusinessException("Stock insuficiente. Actual: " + stockAnterior + ", solicitado: " + cantidad);
                }
                stockNuevo = stockAnterior - cantidad;
                break;
            case AJUSTE:
                stockNuevo = cantidad;
                break;
            default:
                throw new com.autopartes.exception.BusinessException("Tipo de movimiento desconocido");
        }

        stock.setCantidad(stockNuevo);
        stock.setFechaActualizacion(java.time.LocalDateTime.now());
        stockRepository.guardar(stock);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setId(UUID.randomUUID());
        movimiento.setProductoId(productoId);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setMotivo(motivo);
        movimiento.setReferencia(referencia);
        movimiento.setUsuarioId(usuarioId);
        movimiento.setFecha(java.time.LocalDateTime.now());

        return movimientoRepository.guardar(movimiento);
    }

    public long contar() {
        return stockRepository.contar();
    }
}