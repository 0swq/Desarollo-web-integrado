package com.autopartes.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilidadVehiculo {
    private java.util.UUID id;
    private java.util.UUID productoId;
    private java.util.UUID modeloVehiculoId;
    private Integer anioInicio;
    private Integer anioFin;
    private String motor;
    private String notas;
}
