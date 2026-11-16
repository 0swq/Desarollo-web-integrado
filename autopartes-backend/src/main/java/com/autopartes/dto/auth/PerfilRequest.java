package com.autopartes.dto.auth;
import jakarta.validation.constraints.*;
public record PerfilRequest(
    @NotBlank @Size(max=100) String nombre,
    @NotBlank @Size(max=100) String apellido,
    @Size(max=20) @Pattern(regexp="[0-9+() \\-]*", message="El teléfono contiene caracteres inválidos") String telefono,
    @Size(max=255) String direccion,
    @Size(max=2048) @Pattern(regexp="(?:https?://[^\\s]+|/api/storage/imagenes/[^\\s]+)?", message="La foto debe ser una URL válida") String fotoPerfilUrl
) {}
