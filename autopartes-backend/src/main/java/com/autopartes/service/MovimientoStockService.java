package com.autopartes.service;

import com.autopartes.dto.movimientostock.MovimientoStockRequest;
import com.autopartes.model.MovimientoStock;
import com.autopartes.model.TipoMovimiento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class MovimientoStockService {

    private final com.autopartes.repository.MovimientoStockRepository repository;

    public MovimientoStockService(com.autopartes.repository.MovimientoStockRepository repository) {
        this.repository = repository;
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
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setId(UUID.randomUUID());
        movimiento.setProductoId(request.getProductoId());
        movimiento.setTipo(request.getTipo());
        movimiento.setCantidad(request.getCantidad());
        movimiento.setStockAnterior(request.getStockAnterior());
        movimiento.setStockNuevo(request.getStockNuevo());
        movimiento.setMotivo(request.getMotivo());
        movimiento.setReferencia(request.getReferencia());
        movimiento.setUsuarioId(request.getUsuarioId());
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