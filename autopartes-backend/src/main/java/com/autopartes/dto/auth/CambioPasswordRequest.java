package com.autopartes.dto.auth;
import jakarta.validation.constraints.*;
public record CambioPasswordRequest(
    @NotBlank @Size(max=200) String contrasenaActual,
    @NotBlank @Size(min=8,max=72,message="La nueva contraseña debe tener entre 8 y 72 caracteres") String contrasenaNueva,
    @NotBlank @Size(max=72) String confirmacionContrasena
) {}
