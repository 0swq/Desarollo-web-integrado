package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.MarcaVehiculo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class MarcaVehiculoRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<MarcaVehiculo> buscarPorId(UUID id) {
        return data.getMarcasVehiculos().stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    public Optional<MarcaVehiculo> buscarPorNombre(String nombre) {
        return data.getMarcasVehiculos().stream()
                .filter(m -> m.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    public boolean existePorNombre(String nombre) {
        return data.getMarcasVehiculos().stream()
                .anyMatch(m -> m.getNombre().equalsIgnoreCase(nombre));
    }

    public List<MarcaVehiculo> buscarActivas() {
        return data.getMarcasVehiculos().stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .toList();
    }

    public List<MarcaVehiculo> buscarTodos() {
        return new ArrayList<>(data.getMarcasVehiculos());
    }

    public MarcaVehiculo guardar(MarcaVehiculo marca) {
        data.getMarcasVehiculos().removeIf(m -> m.getId().equals(marca.getId()));
        data.getMarcasVehiculos().add(marca);
        return marca;
    }

    public void eliminar(UUID id) {
        data.getMarcasVehiculos().removeIf(m -> m.getId().equals(id));
    }

    public long contar() {
        return data.getMarcasVehiculos().size();
    }
}
