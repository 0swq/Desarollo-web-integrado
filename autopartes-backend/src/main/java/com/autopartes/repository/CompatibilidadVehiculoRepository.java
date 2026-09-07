package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.CompatibilidadVehiculo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class CompatibilidadVehiculoRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<CompatibilidadVehiculo> buscarPorId(UUID id) {
        return data.getCompatibilidadesVehiculos().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    public List<CompatibilidadVehiculo> buscarPorProducto(UUID productoId) {
        return data.getCompatibilidadesVehiculos().stream()
                .filter(c -> c.getProductoId().equals(productoId))
                .toList();
    }

    public List<CompatibilidadVehiculo> buscarPorModeloVehiculo(UUID modeloVehiculoId) {
        return data.getCompatibilidadesVehiculos().stream()
                .filter(c -> c.getModeloVehiculoId().equals(modeloVehiculoId))
                .toList();
    }

    public List<CompatibilidadVehiculo> buscarPorProductoYModelo(UUID productoId, UUID modeloVehiculoId) {
        return data.getCompatibilidadesVehiculos().stream()
                .filter(c -> c.getProductoId().equals(productoId) && c.getModeloVehiculoId().equals(modeloVehiculoId))
                .toList();
    }

    public List<CompatibilidadVehiculo> buscarPorAnio(Integer anio) {
        return data.getCompatibilidadesVehiculos().stream()
                .filter(c -> c.getAnioInicio() <= anio && c.getAnioFin() >= anio)
                .toList();
    }

    public List<CompatibilidadVehiculo> buscarTodos() {
        return new ArrayList<>(data.getCompatibilidadesVehiculos());
    }

    public CompatibilidadVehiculo guardar(CompatibilidadVehiculo compatibilidad) {
        data.getCompatibilidadesVehiculos().removeIf(c -> c.getId().equals(compatibilidad.getId()));
        data.getCompatibilidadesVehiculos().add(compatibilidad);
        return compatibilidad;
    }

    public void eliminar(UUID id) {
        data.getCompatibilidadesVehiculos().removeIf(c -> c.getId().equals(id));
    }

    public void eliminarPorProducto(UUID productoId) {
        data.getCompatibilidadesVehiculos().removeIf(c -> c.getProductoId().equals(productoId));
    }

    public long contar() {
        return data.getCompatibilidadesVehiculos().size();
    }
}
