package com.autopartes.repository;

import com.autopartes.model.Rol;
import com.autopartes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findByRol(Rol rol);

    List<Usuario> findByActivoTrue();


    default Optional<Usuario> buscarPorId(UUID id) {
        return findById(id);
    }

    default Optional<Usuario> buscarPorCorreo(String correo) {
        return findByCorreoIgnoreCase(correo);
    }

    default boolean existePorCorreo(String correo) {
        return existsByCorreoIgnoreCase(correo);
    }

    default List<Usuario> buscarPorRol(Rol rol) {
        return findByRol(rol);
    }

    default List<Usuario> buscarActivos() {
        return findByActivoTrue();
    }

    default List<Usuario> buscarTodos() {
        return findAll();
    }

    default Usuario guardar(Usuario usuario) {
        return save(usuario);
    }

    default void eliminar(UUID id) {
        deleteById(id);
    }

    default long contar() {
        return count();
    }
}