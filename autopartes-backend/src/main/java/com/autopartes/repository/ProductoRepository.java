package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class ProductoRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<Producto> buscarPorId(UUID id) {
        return data.getProductos().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public Optional<Producto> buscarPorSku(String sku) {
        return data.getProductos().stream()
                .filter(p -> p.getSku().equalsIgnoreCase(sku))
                .findFirst();
    }

    public boolean existePorSku(String sku) {
        return data.getProductos().stream()
                .anyMatch(p -> p.getSku().equalsIgnoreCase(sku));
    }

    public List<Producto> buscarPorProveedor(UUID proveedorId) {
        return data.getProductos().stream()
                .filter(p -> p.getProveedorId() != null && p.getProveedorId().equals(proveedorId))
                .toList();
    }

    public List<Producto> buscarActivos() {
        return data.getProductos().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .toList();
    }

    public List<Producto> buscarPorNombreContiene(String texto) {
        String lower = texto.toLowerCase();
        return data.getProductos().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(lower) ||
                        (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(lower)) ||
                        p.getSku().toLowerCase().contains(lower))
                .toList();
    }

    public List<Producto> buscarTodos() {
        return new ArrayList<>(data.getProductos());
    }

    public Producto guardar(Producto producto) {
        data.getProductos().removeIf(p -> p.getId().equals(producto.getId()));
        data.getProductos().add(producto);
        return producto;
    }

    public void eliminar(UUID id) {
        data.getProductos().removeIf(p -> p.getId().equals(id));
    }

    public long contar() {
        return data.getProductos().size();
    }
}
