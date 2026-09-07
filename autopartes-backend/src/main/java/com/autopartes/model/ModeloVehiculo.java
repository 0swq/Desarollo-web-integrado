package com.autopartes.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModeloVehiculo {
    private java.util.UUID id;
    private String nombre;
    private java.util.UUID marcaId;
    private String tipoVehiculo;
    private Boolean activo;
}
