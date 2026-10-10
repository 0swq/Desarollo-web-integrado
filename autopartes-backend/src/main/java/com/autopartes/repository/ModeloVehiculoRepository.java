package com.autopartes.repository;

import com.autopartes.model.ModeloVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModeloVehiculoRepository extends JpaRepository<ModeloVehiculo, UUID> {

    List<ModeloVehiculo> findByMarca_Id(UUID marcaId);

    Optional<ModeloVehiculo> findByMarca_IdAndNombreIgnoreCase(UUID marcaId, String nombre);

    boolean existsByMarca_IdAndNombreIgnoreCase(UUID marcaId, String nombre);

    List<ModeloVehiculo> findByActivoTrue();

    @Transactional
    void deleteByMarca_Id(UUID marcaId);

    default Optional<ModeloVehiculo> buscarPorId(UUID id) {
        return findById(id);
    }

    default List<ModeloVehiculo> buscarPorMarca(UUID marcaId) {
        return findByMarca_Id(marcaId);
    }

    default Optional<ModeloVehiculo> buscarPorMarcaYNombre(UUID marcaId, String nombre) {
        return findByMarca_IdAndNombreIgnoreCase(marcaId, nombre);
    }

    default boolean existePorMarcaYNombre(UUID marcaId, String nombre) {
        return existsByMarca_IdAndNombreIgnoreCase(marcaId, nombre);
    }

    default List<ModeloVehiculo> buscarActivos() {
        return findByActivoTrue();
    }

    default List<ModeloVehiculo> buscarTodos() {
        return findAll();
    }

    default ModeloVehiculo guardar(ModeloVehiculo modelo) {
        return save(modelo);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default void eliminarPorMarca(UUID marcaId) {
        deleteByMarca_Id(marcaId);
    }

    default long contar() {
        return count();
    }
}