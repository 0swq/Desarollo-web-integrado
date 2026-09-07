package com.autopartes.dto.proveedor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorRequest {

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener exactamente 11 dígitos numéricos")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    private String contactoNombre;
    private String telefono;
    private String correo;
    private String direccion;

    @Builder.Default
    private Boolean activo = true;
}
