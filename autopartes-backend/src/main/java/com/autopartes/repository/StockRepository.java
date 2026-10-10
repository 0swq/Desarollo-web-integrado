package com.autopartes.repository;

import com.autopartes.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {

    Optional<Stock> findByProducto_Id(UUID productoId);

    @Transactional
    void deleteByProducto_Id(UUID productoId);

    default Optional<Stock> buscarPorId(UUID id) {
        return findById(id);
    }

    default Optional<Stock> buscarPorProducto(UUID productoId) {
        return findByProducto_Id(productoId);
    }

    default List<Stock> buscarBajoStock() {
        return findAll().stream()
                .filter(s -> s.getCantidad() <= s.getStockMinimo())
                .toList();
    }

    default List<Stock> buscarTodos() {
        return findAll();
    }

    default Stock guardar(Stock stock) {
        return save(stock);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default void eliminarPorProducto(UUID productoId) {
        deleteByProducto_Id(productoId);
    }

    default long contar() {
        return count();
    }
}