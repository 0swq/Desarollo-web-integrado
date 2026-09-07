package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.MovimientoStock;
import com.autopartes.model.TipoMovimiento;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class MovimientoStockRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<MovimientoStock> buscarPorId(UUID id) {
        return data.getMovimientosStock().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    public List<MovimientoStock> buscarPorProducto(UUID productoId) {
        return data.getMovimientosStock().stream()
                .filter(m -> m.getProductoId().equals(productoId))
                .toList();
    }

    public List<MovimientoStock> buscarPorUsuario(UUID usuarioId) {
        return data.getMovimientosStock().stream()
                .filter(m -> m.getUsuarioId() != null && m.getUsuarioId().equals(usuarioId))
                .toList();
    }

    public List<MovimientoStock> buscarPorTipo(TipoMovimiento tipo) {
        return data.getMovimientosStock().stream()
                .filter(m -> m.getTipo() == tipo)
                .toList();
    }

    public List<MovimientoStock> buscarPorProductoYTipo(UUID productoId, TipoMovimiento tipo) {
        return data.getMovimientosStock().stream()
                .filter(m -> m.getProductoId().equals(productoId) && m.getTipo() == tipo)
                .toList();
    }

    public List<MovimientoStock> buscarTodos() {
        return new ArrayList<>(data.getMovimientosStock());
    }

    public MovimientoStock guardar(MovimientoStock movimiento) {
        data.getMovimientosStock().removeIf(m -> m.getId().equals(movimiento.getId()));
        data.getMovimientosStock().add(movimiento);
        return movimiento;
    }

    public void eliminar(UUID id) {
        data.getMovimientosStock().removeIf(m -> m.getId().equals(id));
    }

    public long contar() {
        return data.getMovimientosStock().size();
    }
}
