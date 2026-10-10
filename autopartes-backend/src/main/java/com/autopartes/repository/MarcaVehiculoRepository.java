package com.autopartes.repository;

import com.autopartes.model.MarcaVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarcaVehiculoRepository extends JpaRepository<MarcaVehiculo, UUID> {

    Optional<MarcaVehiculo> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<MarcaVehiculo> findByActivoTrue();

    default Optional<MarcaVehiculo> buscarPorId(UUID id) {
        return findById(id);
    }

    default Optional<MarcaVehiculo> buscarPorNombre(String nombre) {
        return findByNombreIgnoreCase(nombre);
    }

    default boolean existePorNombre(String nombre) {
        return existsByNombreIgnoreCase(nombre);
    }

    default List<MarcaVehiculo> buscarActivas() {
        return findByActivoTrue();
    }

    default List<MarcaVehiculo> buscarTodos() {
        return findAll();
    }

    default MarcaVehiculo guardar(MarcaVehiculo marca) {
        return save(marca);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default long contar() {
        return count();
    }
}