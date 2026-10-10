package com.autopartes.service;

import com.autopartes.dto.movimientostock.MovimientoStockRequest;
import com.autopartes.model.MovimientoStock;
import com.autopartes.model.Producto;
import com.autopartes.model.TipoMovimiento;
import com.autopartes.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class MovimientoStockService {

    private final com.autopartes.repository.MovimientoStockRepository repository;
    private final com.autopartes.repository.ProductoRepository productoRepository;
    private final com.autopartes.repository.UsuarioRepository usuarioRepository;

    public MovimientoStockService(com.autopartes.repository.MovimientoStockRepository repository,
                                  com.autopartes.repository.ProductoRepository productoRepository,
                                  com.autopartes.repository.UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<MovimientoStock> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    public List<MovimientoStock> buscarPorProducto(UUID productoId) {
        return repository.buscarPorProducto(productoId);
    }

    public List<MovimientoStock> buscarPorUsuario(UUID usuarioId) {
        return repository.buscarPorUsuario(usuarioId);
    }

    public List<MovimientoStock> buscarPorTipo(TipoMovimiento tipo) {
        return repository.buscarPorTipo(tipo);
    }

    public List<MovimientoStock> buscarPorProductoYTipo(UUID productoId, TipoMovimiento tipo) {
        return repository.buscarPorProductoYTipo(productoId, tipo);
    }

    public List<MovimientoStock> buscarTodos() {
        return repository.buscarTodos();
    }

    public MovimientoStock crear(MovimientoStockRequest request) {
        Producto producto = productoRepository.buscarPorId(request.getProductoId())
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Producto no encontrado"));

        Usuario usuario = null;
        if (request.getUsuarioId() != null) {
            usuario = usuarioRepository.buscarPorId(request.getUsuarioId())
                    .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));
        }

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setId(UUID.randomUUID());
        movimiento.setProducto(producto);
        movimiento.setTipo(request.getTipo());
        movimiento.setCantidad(request.getCantidad());
        movimiento.setStockAnterior(request.getStockAnterior());
        movimiento.setStockNuevo(request.getStockNuevo());
        movimiento.setMotivo(request.getMotivo());
        movimiento.setReferencia(request.getReferencia());
        movimiento.setUsuario(usuario);
        movimiento.setFecha(java.time.LocalDateTime.now());

        return repository.guardar(movimiento);
    }

    public void eliminar(UUID id) {
        repository.eliminar(id);
    }

    public long contar() {
        return repository.contar();
    }
}