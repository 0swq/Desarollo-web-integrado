package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.auth.UsuarioRequest;
import com.autopartes.dto.auth.UsuarioResponse;
import com.autopartes.model.Usuario;
import com.autopartes.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listar() {
        List<UsuarioResponse> response = usuarioService.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtener(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id)
                .map(u -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(u))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Usuario no encontrado")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(@Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.registrar(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Usuario creado", mapToResponse(usuario)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", mapToResponse(usuario)));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> cambiarContrasena(@PathVariable UUID id, @RequestBody CambioContrasenaRequest request) {
        usuarioService.cambiarContrasena(id, request.getContrasenaActual(), request.getContrasenaNueva());
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada", null));
    }

    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<UsuarioResponse>> toggleActivo(@PathVariable UUID id) {
        usuarioService.toggleActivo(id);
        return usuarioService.buscarPorId(id)
                .map(u -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(u))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Usuario no encontrado")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable UUID id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private static class CambioContrasenaRequest {
        private String contrasenaActual;
        private String contrasenaNueva;
        public String getContrasenaActual() { return contrasenaActual; }
        public void setContrasenaActual(String contrasenaActual) { this.contrasenaActual = contrasenaActual; }
        public String getContrasenaNueva() { return contrasenaNueva; }
        public void setContrasenaNueva(String contrasenaNueva) { this.contrasenaNueva = contrasenaNueva; }
    }

    private UsuarioResponse mapToResponse(Usuario u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setId(u.getId());
        r.setCorreo(u.getCorreo());
        r.setNombre(u.getNombre());
        r.setApellido(u.getApellido());
        r.setTelefono(u.getTelefono());
        r.setDireccion(u.getDireccion());
        r.setRol(u.getRol());
        r.setActivo(u.getActivo());
        r.setFechaCreacion(u.getFechaCreacion());
        r.setFechaActualizacion(u.getFechaActualizacion());
        return r;
    }
}