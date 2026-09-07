package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.ItemCarrito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class ItemCarritoRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<ItemCarrito> buscarPorId(UUID id) {
        return data.getItemsCarrito().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst();
    }

    public List<ItemCarrito> buscarPorCarrito(UUID carritoId) {
        return data.getItemsCarrito().stream()
                .filter(i -> i.getCarritoId().equals(carritoId))
                .toList();
    }

    public Optional<ItemCarrito> buscarPorCarritoYProducto(UUID carritoId, UUID productoId) {
        return data.getItemsCarrito().stream()
                .filter(i -> i.getCarritoId().equals(carritoId) && i.getProductoId().equals(productoId))
                .findFirst();
    }

    public List<ItemCarrito> buscarTodos() {
        return new ArrayList<>(data.getItemsCarrito());
    }

    public ItemCarrito guardar(ItemCarrito item) {
        data.getItemsCarrito().removeIf(i -> i.getId().equals(item.getId()));
        data.getItemsCarrito().add(item);
        return item;
    }

    public void eliminar(UUID id) {
        data.getItemsCarrito().removeIf(i -> i.getId().equals(id));
    }

    public void eliminarPorCarrito(UUID carritoId) {
        data.getItemsCarrito().removeIf(i -> i.getCarritoId().equals(carritoId));
    }

    public long contar() {
        return data.getItemsCarrito().size();
    }
}
