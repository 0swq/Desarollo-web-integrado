package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.Categoria;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class CategoriaRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<Categoria> buscarPorId(UUID id) {
        return data.getCategorias().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    public Optional<Categoria> buscarPorNombre(String nombre) {
        return data.getCategorias().stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }

    public boolean existePorNombre(String nombre) {
        return data.getCategorias().stream()
                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre));
    }

    public List<Categoria> buscarActivas() {
        return data.getCategorias().stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .toList();
    }

    public List<Categoria> buscarTodos() {
        return new ArrayList<>(data.getCategorias());
    }

    public Categoria guardar(Categoria categoria) {
        data.getCategorias().removeIf(c -> c.getId().equals(categoria.getId()));
        data.getCategorias().add(categoria);
        return categoria;
    }

    public void eliminar(UUID id) {
        data.getCategorias().removeIf(c -> c.getId().equals(id));
    }

    public long contar() {
        return data.getCategorias().size();
    }
}
