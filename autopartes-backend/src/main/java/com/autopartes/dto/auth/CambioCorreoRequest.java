package com.autopartes.dto.auth;
import jakarta.validation.constraints.*;
public record CambioCorreoRequest(
    @NotBlank @Size(max=254) @Email(message="Ingresa un correo válido") String correoNuevo,
    @NotBlank @Size(max=254) @Email String confirmacionCorreo,
    @NotBlank @Size(max=200) String contrasenaActual
) {}
