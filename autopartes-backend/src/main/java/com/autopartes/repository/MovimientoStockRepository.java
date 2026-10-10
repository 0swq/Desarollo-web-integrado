package com.autopartes.repository;

import com.autopartes.model.MovimientoStock;
import com.autopartes.model.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, UUID> {

    List<MovimientoStock> findByProducto_Id(UUID productoId);

    List<MovimientoStock> findByUsuario_Id(UUID usuarioId);

    List<MovimientoStock> findByTipo(TipoMovimiento tipo);

    List<MovimientoStock> findByProducto_IdAndTipo(UUID productoId, TipoMovimiento tipo);

    default Optional<MovimientoStock> buscarPorId(UUID id) {
        return findById(id);
    }

    default List<MovimientoStock> buscarPorProducto(UUID productoId) {
        return findByProducto_Id(productoId);
    }

    default List<MovimientoStock> buscarPorUsuario(UUID usuarioId) {
        return findByUsuario_Id(usuarioId);
    }

    default List<MovimientoStock> buscarPorTipo(TipoMovimiento tipo) {
        return findByTipo(tipo);
    }

    default List<MovimientoStock> buscarPorProductoYTipo(UUID productoId, TipoMovimiento tipo) {
        return findByProducto_IdAndTipo(productoId, tipo);
    }

    default List<MovimientoStock> buscarTodos() {
        return findAll();
    }

    default MovimientoStock guardar(MovimientoStock movimiento) {
        return save(movimiento);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default long contar() {
        return count();
    }
}