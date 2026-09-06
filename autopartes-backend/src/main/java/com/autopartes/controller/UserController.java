package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.auth.UserResponse;
import com.autopartes.model.Role;
import com.autopartes.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Endpoints de gestión de usuarios y perfil")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario autenticado actual")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Principal principal) {
        UserResponse user = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios (solo ADMIN)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por ID (solo ADMIN)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(id)));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar rol de usuario (solo ADMIN)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(@PathVariable Long id, @RequestParam Role rol) {
        return ResponseEntity.ok(ApiResponse.ok("Rol actualizado exitosamente", userService.updateUserRole(id, rol)));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar usuario (solo ADMIN)")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado exitosamente", userService.toggleUserStatus(id)));
    }
}
