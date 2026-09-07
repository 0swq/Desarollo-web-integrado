package com.autopartes.dto.proveedor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProveedorResponse {

    private UUID id;
    private String ruc;
    private String razonSocial;
    private String contactoNombre;
    private String telefono;
    private String correo;
    private String direccion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
