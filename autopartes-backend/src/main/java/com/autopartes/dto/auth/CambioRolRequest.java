package com.autopartes.dto.auth;
import com.autopartes.model.Rol;
import jakarta.validation.constraints.NotNull;
public record CambioRolRequest(@NotNull(message="Selecciona un rol válido") Rol rol) {}
