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
        usuario.setFotoPerfilUrl(request.getFotoPerfilUrl() == null || request.getFotoPerfilUrl().isBlank() ? null : request.getFotoPerfilUrl().trim());
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
        if (request.getFotoPerfilUrl() != null) {
            usuario.setFotoPerfilUrl(request.getFotoPerfilUrl().isBlank() ? null : request.getFotoPerfilUrl().trim());
        }
        if (request.getRol() != null) {
            usuario.setRol(request.getRol());
        }
        usuario.setFechaActualizacion(java.time.LocalDateTime.now());

        return repository.guardar(usuario);
    }

    public Usuario cambiarRol(UUID id, Rol rol, String correoAdministrador) {
        Usuario administrador = repository.buscarPorCorreo(correoAdministrador)
            .orElseThrow(() -> new com.autopartes.exception.BusinessException("La sesión no corresponde a un administrador"));
        if (administrador.getRol() != Rol.ADMIN || !Boolean.TRUE.equals(administrador.getActivo()))
            throw new com.autopartes.exception.BusinessException("Solo un administrador activo puede cambiar roles");
        if (id.equals(administrador.getId()))
            throw new com.autopartes.exception.BusinessException("No puedes cambiar tu propio rol desde este panel");
        if (rol == null) throw new com.autopartes.exception.BusinessException("Selecciona un rol válido");
        Usuario usuario = repository.buscarPorId(id)
            .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));
        if (usuario.getRol() == rol) return usuario;
        usuario.setRol(rol);
        usuario.setFechaActualizacion(java.time.LocalDateTime.now());
        return repository.guardar(usuario);
    }

    public Usuario actualizarPerfil(UUID id, com.autopartes.dto.auth.PerfilRequest request) {
        Usuario usuario = repository.buscarPorId(id)
                .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setTelefono(request.telefono() == null ? null : request.telefono().trim());
        usuario.setDireccion(request.direccion() == null ? null : request.direccion().trim());
        usuario.setFotoPerfilUrl(request.fotoPerfilUrl() == null || request.fotoPerfilUrl().isBlank() ? null : request.fotoPerfilUrl().trim());
        usuario.setFechaActualizacion(java.time.LocalDateTime.now());
        return repository.guardar(usuario);
    }

    @org.springframework.transaction.annotation.Transactional
    public Usuario cambiarCorreoPropio(UUID id, com.autopartes.dto.auth.CambioCorreoRequest request) {
        Usuario usuario = repository.buscarPorId(id)
            .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));
        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getContrasena()))
            throw new com.autopartes.exception.BusinessException("La contraseña actual es incorrecta");
        String correo = request.correoNuevo().trim().toLowerCase(java.util.Locale.ROOT);
        if (!correo.equals(request.confirmacionCorreo().trim().toLowerCase(java.util.Locale.ROOT)))
            throw new com.autopartes.exception.BusinessException("Los correos no coinciden");
        if (usuario.getCorreo().equalsIgnoreCase(correo))
            throw new com.autopartes.exception.BusinessException("El nuevo correo debe ser diferente del actual");
        if (repository.existePorCorreo(correo))
            throw new com.autopartes.exception.BusinessException("El correo ya se encuentra registrado");
        usuario.setCorreo(correo);
        usuario.setFechaActualizacion(java.time.LocalDateTime.now());
        try { return repository.saveAndFlush(usuario); }
        catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new com.autopartes.exception.BusinessException("No se pudo cambiar el correo; comprueba que no esté registrado");
        }
    }

    public void cambiarPasswordPropio(UUID id, com.autopartes.dto.auth.CambioPasswordRequest request) {
        if (!request.contrasenaNueva().equals(request.confirmacionContrasena()))
            throw new com.autopartes.exception.BusinessException("Las contraseñas no coinciden");
        if (request.contrasenaNueva().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new com.autopartes.exception.BusinessException("La contraseña supera el tamaño permitido de 72 bytes");
        Usuario usuario = repository.buscarPorId(id)
            .orElseThrow(() -> new com.autopartes.exception.ResourceNotFoundException("Usuario no encontrado"));
        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getContrasena()))
            throw new com.autopartes.exception.BusinessException("La contraseña actual es incorrecta");
        if (passwordEncoder.matches(request.contrasenaNueva(), usuario.getContrasena()))
            throw new com.autopartes.exception.BusinessException("La nueva contraseña debe ser diferente de la actual");
        usuario.setContrasena(passwordEncoder.encode(request.contrasenaNueva()));
        usuario.setFechaActualizacion(java.time.LocalDateTime.now());
        repository.guardar(usuario);
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