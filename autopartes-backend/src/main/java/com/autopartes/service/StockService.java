package com.autopartes.service;

import com.autopartes.dto.stock.StockRequest;
import com.autopartes.model.MovimientoStock;
import com.autopartes.model.Producto;
import com.autopartes.model.Stock;
import com.autopartes.model.TipoMovimiento;
import com.autopartes.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
@org.springframework.transaction.annotation.Transactional
public class StockService {

    private final com.autopartes.repository.StockRepository stockRepository;
    private final com.autopartes.repository.MovimientoStockRepository movimientoRepository;
    private final com.autopartes.repository.ProductoRepository productoRepository;
    private final com.autopartes.repository.UsuarioRepository usuarioRepository;

    public StockService(com.autopartes.repository.StockRepository stockRepository,
                        com.autopartes.repository.MovimientoStockRepository movimientoRepository,
                        com.autopartes.repository.ProductoRepository productoRepository,
                        com.autopartes.repository.UsuarioRepository usuarioRepository) {
        this.stockRepository = stockRepository;
        this.movimientoRepository = movimientoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
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
        Producto producto = productoRepository.bloquearPorId(productoId)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Producto no encontrado"));

        Stock stock = stockRepository.buscarPorProducto(productoId)
                .orElseGet(() -> {
                    Stock nuevo = new Stock();
                    nuevo.setId(UUID.randomUUID());
                    nuevo.setProducto(producto);
                    nuevo.setCantidad(0);
                    nuevo.setStockMinimo(5);
                    nuevo.setFechaActualizacion(java.time.LocalDateTime.now());
                    return stockRepository.guardar(nuevo);
                });

        if(cantidad==null || cantidad<0 || (tipo!=TipoMovimiento.AJUSTE && cantidad==0)) throw new com.autopartes.exception.BusinessException("Cantidad inválida");
        int stockAnterior = stock.getCantidad();
        int stockNuevo;

        switch (tipo) {
            case ENTRADA:
                if((long)stockAnterior+cantidad>Integer.MAX_VALUE) throw new com.autopartes.exception.BusinessException("Cantidad fuera de rango"); stockNuevo = stockAnterior + cantidad;
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
        movimiento.setProducto(producto);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockNuevo(stockNuevo);
        movimiento.setMotivo(motivo);
        movimiento.setReferencia(referencia);
        movimiento.setUsuario(usuarioId != null
                ? usuarioRepository.buscarPorId(usuarioId)
                        .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"))
                : null);
        movimiento.setFecha(java.time.LocalDateTime.now());

        return movimientoRepository.guardar(movimiento);
    }

    public MovimientoStock manual(UUID productoId, TipoMovimiento tipo, Integer cantidad, String motivo, String referencia, String confirmacion, Integer esperado, String correo) {
        if(!"confirmo hacer este movimiento".equals(confirmacion)) throw new com.autopartes.exception.BusinessException("Escribe la frase de confirmación exacta");
        if(motivo==null || motivo.trim().length()<5 || motivo.length()>255 || (referencia!=null && referencia.length()>255)) throw new com.autopartes.exception.BusinessException("Revisa el motivo y la referencia");
        productoRepository.bloquearPorId(productoId).orElseThrow(()->new com.autopartes.exception.ResourceNotFoundException("Producto no encontrado"));
        Stock stock=stockRepository.buscarPorProducto(productoId).orElseThrow(()->new com.autopartes.exception.ResourceNotFoundException("Stock no encontrado"));
        if(!java.util.Objects.equals(esperado,stock.getCantidad())) throw new com.autopartes.exception.BusinessException("El stock cambió. Actualiza antes de confirmar");
        if(tipo==TipoMovimiento.AJUSTE && java.util.Objects.equals(cantidad,esperado)) throw new com.autopartes.exception.BusinessException("El ajuste debe cambiar el stock actual");
        Usuario usuario=usuarioRepository.buscarPorCorreo(correo).orElseThrow(()->new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));
        return registrarMovimiento(productoId,tipo,cantidad,motivo.trim(),referencia,usuario.getId());
    }
    public long contar() {
        return stockRepository.contar();
    }
}