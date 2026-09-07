package com.autopartes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {
    private java.util.UUID id;
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
