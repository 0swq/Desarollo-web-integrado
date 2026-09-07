package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.auth.JwtAuthResponse;
import com.autopartes.dto.auth.LoginRequest;
import com.autopartes.dto.auth.UsuarioRequest;
import com.autopartes.dto.auth.UsuarioResponse;
import com.autopartes.model.Rol;
import com.autopartes.model.Usuario;
import com.autopartes.service.UsuarioService;
import com.autopartes.util.Auth;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        var usuarioOpt = usuarioService.validarCredenciales(request.getCorreo(), request.getContrasena());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Credenciales inválidas"));
        }
        Usuario usuario = usuarioOpt.get();
        String token = Auth.crearToken(usuario.getId(), usuario.getRol());
        JwtAuthResponse response = JwtAuthResponse.builder()
                .token(token)
                .id(usuario.getId())
                .correo(usuario.getCorreo())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .rol(usuario.getRol())
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UsuarioResponse>> register(@Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.registrar(request);
        UsuarioResponse response = new UsuarioResponse();
        response.setId(usuario.getId());
        response.setCorreo(usuario.getCorreo());
        response.setNombre(usuario.getNombre());
        response.setApellido(usuario.getApellido());
        response.setTelefono(usuario.getTelefono());
        response.setDireccion(usuario.getDireccion());
        response.setRol(usuario.getRol());
        response.setActivo(usuario.getActivo());
        response.setFechaCreacion(usuario.getFechaCreacion());
        response.setFechaActualizacion(usuario.getFechaActualizacion());
        return ResponseEntity.status(201).body(ApiResponse.ok("Usuario registrado exitosamente", response));
    }
}