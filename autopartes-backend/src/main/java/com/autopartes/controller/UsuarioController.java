package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.auth.UsuarioRequest;
import com.autopartes.dto.auth.UsuarioResponse;
import com.autopartes.model.Usuario;
import com.autopartes.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Usuarios", description = "Gestion de usuarios")
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios registrados")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listar() {
        List<UsuarioResponse> response = usuarioService.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Obtener usuario", description = "Devuelve un usuario por su identificador")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtener(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id)
                .map(u -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(u))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Usuario no encontrado")));
    }

    @Operation(summary = "Crear usuario", description = "Registra un usuario. El correo debe ser unico")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Recurso creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(@Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.registrar(request);
        return ResponseEntity.status(201).body(ApiResponse.ok("Usuario creado", mapToResponse(usuario)));
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos de un usuario")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", mapToResponse(usuario)));
    }

    @Operation(summary = "Cambiar contrasena", description = "Cambia la contrasena validando la actual")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o regla de negocio incumplida"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> cambiarContrasena(@PathVariable UUID id, @RequestBody CambioContrasenaRequest request) {
        usuarioService.cambiarContrasena(id, request.getContrasenaActual(), request.getContrasenaNueva());
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada", null));
    }

    @Operation(summary = "Activar o desactivar usuario", description = "Invierte el estado activo del usuario")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operacion exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<UsuarioResponse>> toggleActivo(@PathVariable UUID id) {
        usuarioService.toggleActivo(id);
        return usuarioService.buscarPorId(id)
                .map(u -> ResponseEntity.ok(ApiResponse.ok("Estado cambiado", mapToResponse(u))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Usuario no encontrado")));
    }

    @Operation(summary = "Eliminar usuario", description = "Baja logica: marca el usuario como inactivo")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Operacion completada sin contenido"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recurso no encontrado")
    })
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