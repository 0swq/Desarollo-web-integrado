package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.Proveedor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class ProveedorRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<Proveedor> buscarPorId(UUID id) {
        return data.getProveedores().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public Optional<Proveedor> buscarPorRuc(String ruc) {
        return data.getProveedores().stream()
                .filter(p -> p.getRuc().equals(ruc))
                .findFirst();
    }

    public boolean existePorRuc(String ruc) {
        return data.getProveedores().stream()
                .anyMatch(p -> p.getRuc().equals(ruc));
    }

    public List<Proveedor> buscarActivos() {
        return data.getProveedores().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .toList();
    }

    public List<Proveedor> buscarTodos() {
        return new ArrayList<>(data.getProveedores());
    }

    public Proveedor guardar(Proveedor proveedor) {
        data.getProveedores().removeIf(p -> p.getId().equals(proveedor.getId()));
        data.getProveedores().add(proveedor);
        return proveedor;
    }

    public void eliminar(UUID id) {
        data.getProveedores().removeIf(p -> p.getId().equals(id));
    }

    public long contar() {
        return data.getProveedores().size();
    }
}
