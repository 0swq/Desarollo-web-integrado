package com.autopartes.repository;

import com.autopartes.model.EstadoOrden;
import com.autopartes.DataStore;
import com.autopartes.model.Orden;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class OrdenRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<Orden> buscarPorId(UUID id) {
        return data.getOrdenes().stream()
                .filter(o -> o.getId().equals(id))
                .findFirst();
    }

    public Optional<Orden> buscarPorNumeroOrden(String numeroOrden) {
        return data.getOrdenes().stream()
                .filter(o -> o.getNumeroOrden().equalsIgnoreCase(numeroOrden))
                .findFirst();
    }

    public List<Orden> buscarPorUsuario(UUID usuarioId) {
        return data.getOrdenes().stream()
                .filter(o -> o.getUsuarioId().equals(usuarioId))
                .toList();
    }

    public List<Orden> buscarPorEstado(EstadoOrden estado) {
        return data.getOrdenes().stream()
                .filter(o -> o.getEstado() == estado)
                .toList();
    }

    public List<Orden> buscarTodos() {
        return new ArrayList<>(data.getOrdenes());
    }

    public Orden guardar(Orden orden) {
        data.getOrdenes().removeIf(o -> o.getId().equals(orden.getId()));
        data.getOrdenes().add(orden);
        return orden;
    }

    public void eliminar(UUID id) {
        data.getOrdenes().removeIf(o -> o.getId().equals(id));
    }

    public long contar() {
        return data.getOrdenes().size();
    }

    public long contarPorEstado(EstadoOrden estado) {
        return data.getOrdenes().stream()
                .filter(o -> o.getEstado() == estado)
                .count();
    }
}
