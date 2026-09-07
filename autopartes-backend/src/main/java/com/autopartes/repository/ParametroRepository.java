package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.Parametro;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class ParametroRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<Parametro> buscarPorId(UUID id) {
        return data.getParametros().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public Optional<Parametro> buscarPorClave(String clave) {
        return data.getParametros().stream()
                .filter(p -> p.getClave().equalsIgnoreCase(clave))
                .findFirst();
    }

    public boolean existePorClave(String clave) {
        return data.getParametros().stream()
                .anyMatch(p -> p.getClave().equalsIgnoreCase(clave));
    }

    public List<Parametro> buscarTodos() {
        return new ArrayList<>(data.getParametros());
    }

    public Parametro guardar(Parametro parametro) {
        data.getParametros().removeIf(p -> p.getId().equals(parametro.getId()));
        data.getParametros().add(parametro);
        return parametro;
    }

    public void eliminar(UUID id) {
        data.getParametros().removeIf(p -> p.getId().equals(id));
    }

    public void eliminarPorClave(String clave) {
        data.getParametros().removeIf(p -> p.getClave().equalsIgnoreCase(clave));
    }

    public long contar() {
        return data.getParametros().size();
    }
}
