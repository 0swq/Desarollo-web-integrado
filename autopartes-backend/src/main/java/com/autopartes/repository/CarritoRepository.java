package com.autopartes.repository;

import com.autopartes.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Carrito c where c.usuario.id = :id")
    Optional<Carrito> bloquearPorUsuario(@org.springframework.data.repository.query.Param("id") UUID id);


    Optional<Carrito> findByUsuario_Id(UUID usuarioId);

    @Transactional
    void deleteByUsuario_Id(UUID usuarioId);

    default Optional<Carrito> buscarPorId(UUID id) {
        return findById(id);
    }

    default Optional<Carrito> buscarPorUsuario(UUID usuarioId) {
        return findByUsuario_Id(usuarioId);
    }

    default List<Carrito> buscarTodos() {
        return findAll();
    }

    default Carrito guardar(Carrito carrito) {
        return save(carrito);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default void eliminarPorUsuario(UUID usuarioId) {
        deleteByUsuario_Id(usuarioId);
    }

    default long contar() {
        return count();
    }
}