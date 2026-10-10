package com.autopartes.repository;

import com.autopartes.model.EstadoOrden;
import com.autopartes.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, UUID> {

    Optional<Orden> findByNumeroOrdenIgnoreCase(String numeroOrden);

    List<Orden> findByUsuario_Id(UUID usuarioId);

    List<Orden> findByEstado(EstadoOrden estado);

    long countByEstado(EstadoOrden estado);

    default Optional<Orden> buscarPorId(UUID id) {
        return findById(id);
    }

    default Optional<Orden> buscarPorNumeroOrden(String numeroOrden) {
        return findByNumeroOrdenIgnoreCase(numeroOrden);
    }

    default List<Orden> buscarPorUsuario(UUID usuarioId) {
        return findByUsuario_Id(usuarioId);
    }

    default List<Orden> buscarPorEstado(EstadoOrden estado) {
        return findByEstado(estado);
    }

    default List<Orden> buscarTodos() {
        return findAll();
    }

    default Orden guardar(Orden orden) {
        return save(orden);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default long contar() {
        return count();
    }

    default long contarPorEstado(EstadoOrden estado) {
        return countByEstado(estado);
    }
}