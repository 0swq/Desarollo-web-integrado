package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.Carrito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class CarritoRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<Carrito> buscarPorId(UUID id) {
        return data.getCarritos().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    public Optional<Carrito> buscarPorUsuario(UUID usuarioId) {
        return data.getCarritos().stream()
                .filter(c -> c.getUsuarioId().equals(usuarioId))
                .findFirst();
    }

    public List<Carrito> buscarTodos() {
        return new ArrayList<>(data.getCarritos());
    }

    public Carrito guardar(Carrito carrito) {
        data.getCarritos().removeIf(c -> c.getId().equals(carrito.getId()));
        data.getCarritos().add(carrito);
        return carrito;
    }

    public void eliminar(UUID id) {
        data.getCarritos().removeIf(c -> c.getId().equals(id));
    }

    public void eliminarPorUsuario(UUID usuarioId) {
        data.getCarritos().removeIf(c -> c.getUsuarioId().equals(usuarioId));
    }

    public long contar() {
        return data.getCarritos().size();
    }
}
