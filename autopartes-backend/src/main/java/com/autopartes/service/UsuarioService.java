package com.autopartes.service;

import com.autopartes.dto.auth.UsuarioRequest;
import com.autopartes.model.Rol;
import com.autopartes.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final com.autopartes.repository.UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(com.autopartes.repository.UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrar(UsuarioRequest request) {
        if (repository.existePorCorreo(request.getCorreo())) {
            throw new com.autopartes.exception.BusinessException("El correo ya se encuentra registrado");
        }

        Rol rol = request.getRol() != null ? request.getRol() : Rol.CLIENTE;

        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setCorreo(request.getCorreo().trim().toLowerCase());
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setFechaCreacion(java.time.LocalDateTime.now());
        usuario.setFechaActualizacion(java.time.LocalDateTime.now());

        return repository.guardar(usuario);
    }

    public Optional<Usuario> validarCredenciales(String correo, String contrasena) {
        Optional<Usuario> usuarioOpt = repository.buscarPorCorreo(correo.trim().toLowerCase());
        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }
        Usuario usuario = usuarioOpt.get();
        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
            return Optional.empty();
        }
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return Optional.empty();
        }
        return Optional.of(usuario);
    }

    public Optional<Usuario> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return repository.buscarPorCorreo(correo);
    }

    public List<Usuario> buscarTodos() {
        return repository.buscarTodos();
    }

    public List<Usuario> buscarPorRol(Rol rol) {
        return repository.buscarPorRol(rol);
    }

    public List<Usuario> buscarActivos() {
        return repository.buscarActivos();
    }

    public Usuario actualizar(UUID id, UsuarioRequest request) {
        Usuario usuario = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.getCorreo().equalsIgnoreCase(request.getCorreo()) && repository.existePorCorreo(request.getCorreo())) {
            throw new com.autopartes.exception.BusinessException("El correo ya se encuentra registrado");
        }

        usuario.setCorreo(request.getCorreo().trim().toLowerCase());
        usuario.setNombre(request.getNombre().trim());
        usuario.setApellido(request.getApellido().trim());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        if (request.getRol() != null) {
            usuario.setRol(request.getRol());
        }
        usuario.setFechaActualizacion(java.time.LocalDateTime.now());

        return repository.guardar(usuario);
    }

    public void cambiarContrasena(UUID id, String contrasenaActual, String contrasenaNueva) {
        Usuario usuario = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
            throw new com.autopartes.exception.BusinessException("La contraseña actual es incorrecta");
        }

        usuario.setContrasena(passwordEncoder.encode(contrasenaNueva));
        usuario.setFechaActualizacion(java.time.LocalDateTime.now());
        repository.guardar(usuario);
    }

    public void eliminar(UUID id) {
        repository.eliminar(id);
    }

    public void toggleActivo(UUID id) {
        Usuario usuario = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));
        usuario.setActivo(!Boolean.TRUE.equals(usuario.getActivo()));
        usuario.setFechaActualizacion(java.time.LocalDateTime.now());
        repository.guardar(usuario);
    }

    public long contar() {
        return repository.contar();
    }
}