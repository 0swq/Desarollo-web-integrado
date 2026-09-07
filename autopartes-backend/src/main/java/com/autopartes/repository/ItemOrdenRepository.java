package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.ItemOrden;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class ItemOrdenRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<ItemOrden> buscarPorId(UUID id) {
        return data.getItemsOrden().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst();
    }

    public List<ItemOrden> buscarPorOrden(UUID ordenId) {
        return data.getItemsOrden().stream()
                .filter(i -> i.getOrdenId().equals(ordenId))
                .toList();
    }

    public List<ItemOrden> buscarTodos() {
        return new ArrayList<>(data.getItemsOrden());
    }

    public ItemOrden guardar(ItemOrden item) {
        data.getItemsOrden().removeIf(i -> i.getId().equals(item.getId()));
        data.getItemsOrden().add(item);
        return item;
    }

    public void eliminar(UUID id) {
        data.getItemsOrden().removeIf(i -> i.getId().equals(id));
    }

    public void eliminarPorOrden(UUID ordenId) {
        data.getItemsOrden().removeIf(i -> i.getOrdenId().equals(ordenId));
    }

    public long contar() {
        return data.getItemsOrden().size();
    }
}
