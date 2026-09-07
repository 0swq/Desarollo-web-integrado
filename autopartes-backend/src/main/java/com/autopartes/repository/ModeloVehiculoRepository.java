package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.ModeloVehiculo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class ModeloVehiculoRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<ModeloVehiculo> buscarPorId(UUID id) {
        return data.getModelosVehiculos().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    public List<ModeloVehiculo> buscarPorMarca(UUID marcaId) {
        return data.getModelosVehiculos().stream()
                .filter(m -> m.getMarcaId().equals(marcaId))
                .toList();
    }

    public Optional<ModeloVehiculo> buscarPorMarcaYNombre(UUID marcaId, String nombre) {
        return data.getModelosVehiculos().stream()
                .filter(m -> m.getMarcaId().equals(marcaId) && m.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    public boolean existePorMarcaYNombre(UUID marcaId, String nombre) {
        return data.getModelosVehiculos().stream()
                .anyMatch(m -> m.getMarcaId().equals(marcaId) && m.getNombre().equalsIgnoreCase(nombre));
    }

    public List<ModeloVehiculo> buscarActivos() {
        return data.getModelosVehiculos().stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .toList();
    }

    public List<ModeloVehiculo> buscarTodos() {
        return new ArrayList<>(data.getModelosVehiculos());
    }

    public ModeloVehiculo guardar(ModeloVehiculo modelo) {
        data.getModelosVehiculos().removeIf(m -> m.getId().equals(modelo.getId()));
        data.getModelosVehiculos().add(modelo);
        return modelo;
    }

    public void eliminar(UUID id) {
        data.getModelosVehiculos().removeIf(m -> m.getId().equals(id));
    }

    public void eliminarPorMarca(UUID marcaId) {
        data.getModelosVehiculos().removeIf(m -> m.getMarcaId().equals(marcaId));
    }

    public long contar() {
        return data.getModelosVehiculos().size();
    }
}
