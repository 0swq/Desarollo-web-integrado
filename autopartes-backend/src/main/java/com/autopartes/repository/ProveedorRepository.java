package com.autopartes.repository;

import com.autopartes.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, UUID> {

    Optional<Proveedor> findByRuc(String ruc);

    boolean existsByRuc(String ruc);

    List<Proveedor> findByActivoTrue();

    default Optional<Proveedor> buscarPorId(UUID id) {
        return findById(id);
    }

    default Optional<Proveedor> buscarPorRuc(String ruc) {
        return findByRuc(ruc);
    }

    default boolean existePorRuc(String ruc) {
        return existsByRuc(ruc);
    }

    default List<Proveedor> buscarActivos() {
        return findByActivoTrue();
    }

    default List<Proveedor> buscarTodos() {
        return findAll();
    }

    default Proveedor guardar(Proveedor proveedor) {
        return save(proveedor);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default long contar() {
        return count();
    }
}