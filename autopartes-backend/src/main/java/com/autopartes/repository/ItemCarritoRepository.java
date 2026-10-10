package com.autopartes.repository;

import com.autopartes.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, UUID> {

    List<ItemCarrito> findByCarrito_Id(UUID carritoId);

    Optional<ItemCarrito> findByCarrito_IdAndProducto_Id(UUID carritoId, UUID productoId);

    @Transactional
    void deleteByCarrito_Id(UUID carritoId);

    default Optional<ItemCarrito> buscarPorId(UUID id) {
        return findById(id);
    }

    default List<ItemCarrito> buscarPorCarrito(UUID carritoId) {
        return findByCarrito_Id(carritoId);
    }

    default Optional<ItemCarrito> buscarPorCarritoYProducto(UUID carritoId, UUID productoId) {
        return findByCarrito_IdAndProducto_Id(carritoId, productoId);
    }

    default List<ItemCarrito> buscarTodos() {
        return findAll();
    }

    default ItemCarrito guardar(ItemCarrito item) {
        return save(item);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default void eliminarPorCarrito(UUID carritoId) {
        deleteByCarrito_Id(carritoId);
    }

    default long contar() {
        return count();
    }
}