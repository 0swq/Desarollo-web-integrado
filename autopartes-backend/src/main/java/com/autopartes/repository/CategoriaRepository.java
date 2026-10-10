package com.autopartes.repository;

import com.autopartes.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<Categoria> findByActivoTrue();

    default Optional<Categoria> buscarPorId(UUID id) {
        return findById(id);
    }

    default Optional<Categoria> buscarPorNombre(String nombre) {
        return findByNombreIgnoreCase(nombre);
    }

    default boolean existePorNombre(String nombre) {
        return existsByNombreIgnoreCase(nombre);
    }

    default List<Categoria> buscarActivas() {
        return findByActivoTrue();
    }

    default List<Categoria> buscarTodos() {
        return findAll();
    }

    default Categoria guardar(Categoria categoria) {
        return save(categoria);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default long contar() {
        return count();
    }
}