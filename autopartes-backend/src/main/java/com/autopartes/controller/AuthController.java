package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.auth.JwtAuthResponse;
import com.autopartes.dto.auth.LoginRequest;
import com.autopartes.dto.auth.UsuarioRequest;
import com.autopartes.dto.auth.UsuarioResponse;
import com.autopartes.model.Usuario;
import com.autopartes.service.JwtService;
import com.autopartes.service.UsuarioService;
import com.autopartes.service.UsuarioSecurityDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Registro e inicio de sesion")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UsuarioService usuarioService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "Iniciar sesion", description = "Valida las credenciales y devuelve el token JWT para las peticiones protegidas")
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales invalidas")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena())
            );
            UsuarioSecurityDetails userDetails = (UsuarioSecurityDetails) authentication.getPrincipal();
            Usuario usuario = usuarioService.buscarPorCorreo(request.getCorreo())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            String token = jwtService.generateToken(usuario.getCorreo());
            JwtAuthResponse response = JwtAuthResponse.builder()
                    .token(token)
                    .id(usuario.getId())
                    .correo(usuario.getCorreo())
                    .nombre(usuario.getNombre())
                    .apellido(usuario.getApellido())
                    .rol(usuario.getRol())
                    .build();
            return ResponseEntity.ok(ApiResponse.ok("Login exitoso", response));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(ApiResponse.error("Credenciales inválidas"));
        }
    }

    @Operation(summary = "Registrar usuario", description = "Crea una cuenta nueva. El correo debe ser unico")
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o correo ya registrado")
    })
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