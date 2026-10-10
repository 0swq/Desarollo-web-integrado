package com.autopartes.repository;

import com.autopartes.model.ItemOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemOrdenRepository extends JpaRepository<ItemOrden, UUID> {

    List<ItemOrden> findByOrden_Id(UUID ordenId);

    @Transactional
    void deleteByOrden_Id(UUID ordenId);

    default Optional<ItemOrden> buscarPorId(UUID id) {
        return findById(id);
    }

    default List<ItemOrden> buscarPorOrden(UUID ordenId) {
        return findByOrden_Id(ordenId);
    }

    default List<ItemOrden> buscarTodos() {
        return findAll();
    }

    default ItemOrden guardar(ItemOrden item) {
        return save(item);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default void eliminarPorOrden(UUID ordenId) {
        deleteByOrden_Id(ordenId);
    }

    default long contar() {
        return count();
    }
}