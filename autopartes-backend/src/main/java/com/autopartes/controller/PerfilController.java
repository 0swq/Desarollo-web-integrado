package com.autopartes.controller;
import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.auth.PerfilRequest;
import com.autopartes.dto.auth.UsuarioResponse;
import com.autopartes.dto.itemorden.ItemOrdenResponse;
import com.autopartes.model.Usuario;
import com.autopartes.service.*;
import com.autopartes.storage.SupabaseStorageService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {
    private final UsuarioService usuarios;
    private final OrdenService ordenes;
    private final ItemOrdenService items;
    private final SupabaseStorageService storage;
    private final JwtService jwt;
    public PerfilController(UsuarioService usuarios, OrdenService ordenes, ItemOrdenService items, SupabaseStorageService storage, JwtService jwt) {
        this.usuarios=usuarios; this.ordenes=ordenes; this.items=items; this.storage=storage; this.jwt=jwt;
    }
    private Usuario actual(Principal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return usuarios.buscarPorCorreo(principal.getName()).filter(u -> Boolean.TRUE.equals(u.getActivo()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
    @GetMapping
    public ApiResponse<UsuarioResponse> obtener(Principal principal) { return ApiResponse.ok(respuesta(actual(principal))); }
    @PutMapping
    public ApiResponse<UsuarioResponse> actualizar(Principal principal, @Valid @RequestBody PerfilRequest request) {
        return ApiResponse.ok("Perfil actualizado", respuesta(usuarios.actualizarPerfil(actual(principal).getId(), request)));
    }
    @PostMapping(value="/foto", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UsuarioResponse> foto(Principal principal, @RequestPart("archivo") MultipartFile archivo) {
        Usuario u=actual(principal);
        String url=storage.crear(archivo, "perfiles").url();
        var datos=new PerfilRequest(u.getNombre(), u.getApellido(), u.getTelefono(), u.getDireccion(), url);
        return ApiResponse.ok("Foto actualizada", respuesta(usuarios.actualizarPerfil(u.getId(), datos)));
    }
    @PatchMapping("/password")
    public ApiResponse<Void> cambiarPassword(Principal principal, @Valid @RequestBody com.autopartes.dto.auth.CambioPasswordRequest request) {
        usuarios.cambiarPasswordPropio(actual(principal).getId(), request);
        return ApiResponse.ok("Contraseña actualizada", null);
    }
    @PatchMapping("/correo")
    public ApiResponse<com.autopartes.dto.auth.JwtAuthResponse> cambiarCorreo(Principal principal, @Valid @RequestBody com.autopartes.dto.auth.CambioCorreoRequest request) {
        Usuario u = usuarios.cambiarCorreoPropio(actual(principal).getId(), request);
        var sesion = com.autopartes.dto.auth.JwtAuthResponse.builder().token(jwt.generateToken(u.getCorreo()))
            .id(u.getId()).correo(u.getCorreo()).nombre(u.getNombre()).apellido(u.getApellido())
            .fotoPerfilUrl(u.getFotoPerfilUrl()).rol(u.getRol()).build();
        return ApiResponse.ok("Correo actualizado", sesion);
    }
    @GetMapping("/pedidos/{id}/items")
    @Transactional(readOnly=true)
    public ApiResponse<List<ItemOrdenResponse>> detalle(Principal principal, @PathVariable UUID id) {
        Usuario u=actual(principal);
        var orden=ordenes.buscarPorId(id).filter(o -> u.getId().equals(o.getUsuarioId()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        return ApiResponse.ok(items.buscarPorOrden(orden.getId()).stream().map(i -> ItemOrdenResponse.builder()
            .id(i.getId()).ordenId(i.getOrdenId()).productoId(i.getProductoId()).nombreProducto(i.getNombreProducto())
            .sku(i.getSku()).cantidad(i.getCantidad()).precioUnitario(i.getPrecioUnitario()).subtotal(i.getSubtotal()).build()).toList());
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> error(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ApiResponse.error(ex.getReason() == null ? "No autorizado" : ex.getReason()));
    }
    private UsuarioResponse respuesta(Usuario u) {
        return UsuarioResponse.builder().id(u.getId()).correo(u.getCorreo()).nombre(u.getNombre()).apellido(u.getApellido())
            .telefono(u.getTelefono()).direccion(u.getDireccion()).fotoPerfilUrl(u.getFotoPerfilUrl()).rol(u.getRol())
            .activo(u.getActivo()).fechaCreacion(u.getFechaCreacion()).fechaActualizacion(u.getFechaActualizacion()).build();
    }
}
