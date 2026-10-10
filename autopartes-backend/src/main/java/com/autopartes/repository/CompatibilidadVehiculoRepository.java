package com.autopartes.repository;

import com.autopartes.model.CompatibilidadVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompatibilidadVehiculoRepository extends JpaRepository<CompatibilidadVehiculo, UUID> {

    List<CompatibilidadVehiculo> findByProducto_Id(UUID productoId);

    List<CompatibilidadVehiculo> findByModeloVehiculo_Id(UUID modeloVehiculoId);

    List<CompatibilidadVehiculo> findByProducto_IdAndModeloVehiculo_Id(UUID productoId, UUID modeloVehiculoId);

    @Query("SELECT c FROM CompatibilidadVehiculo c "
            + "WHERE (c.anioInicio IS NULL OR c.anioInicio <= :anio) "
            + "AND (c.anioFin IS NULL OR c.anioFin >= :anio)")
    List<CompatibilidadVehiculo> buscarPorAnioRango(@Param("anio") Integer anio);

    @Transactional
    void deleteByProducto_Id(UUID productoId);

    default Optional<CompatibilidadVehiculo> buscarPorId(UUID id) {
        return findById(id);
    }

    default List<CompatibilidadVehiculo> buscarPorProducto(UUID productoId) {
        return findByProducto_Id(productoId);
    }

    default List<CompatibilidadVehiculo> buscarPorModeloVehiculo(UUID modeloVehiculoId) {
        return findByModeloVehiculo_Id(modeloVehiculoId);
    }

    default List<CompatibilidadVehiculo> buscarPorProductoYModelo(UUID productoId, UUID modeloVehiculoId) {
        return findByProducto_IdAndModeloVehiculo_Id(productoId, modeloVehiculoId);
    }

    default List<CompatibilidadVehiculo> buscarPorAnio(Integer anio) {
        return buscarPorAnioRango(anio);
    }

    default List<CompatibilidadVehiculo> buscarTodos() {
        return findAll();
    }

    default CompatibilidadVehiculo guardar(CompatibilidadVehiculo compatibilidad) {
        return save(compatibilidad);
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