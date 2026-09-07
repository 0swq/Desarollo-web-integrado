package com.autopartes.repository;

import com.autopartes.DataStore;
import com.autopartes.model.Rol;
import com.autopartes.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepository {

    private final DataStore data = DataStore.obtenerInstancia();

    public Optional<Usuario> buscarPorId(UUID id) {
        return data.getUsuarios().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return data.getUsuarios().stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
    }

    public boolean existePorCorreo(String correo) {
        return data.getUsuarios().stream()
                .anyMatch(u -> u.getCorreo().equalsIgnoreCase(correo));
    }

    public List<Usuario> buscarPorRol(Rol rol) {
        return data.getUsuarios().stream()
                .filter(u -> u.getRol() == rol)
                .toList();
    }

    public List<Usuario> buscarActivos() {
        return data.getUsuarios().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .toList();
    }

    public List<Usuario> buscarTodos() {
        return new ArrayList<>(data.getUsuarios());
    }

    public Usuario guardar(Usuario usuario) {
        data.getUsuarios().removeIf(u -> u.getId().equals(usuario.getId()));
        data.getUsuarios().add(usuario);
        return usuario;
    }

    public void eliminar(UUID id) {
        data.getUsuarios().removeIf(u -> u.getId().equals(id));
    }

    public long contar() {
        return data.getUsuarios().size();
    }
}
