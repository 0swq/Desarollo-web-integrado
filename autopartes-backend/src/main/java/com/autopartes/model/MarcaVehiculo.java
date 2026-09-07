package com.autopartes.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarcaVehiculo {
    private java.util.UUID id;
    private String nombre;
    private String paisOrigen;
    private Boolean activo;
}
