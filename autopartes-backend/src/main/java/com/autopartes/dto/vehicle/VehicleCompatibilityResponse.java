package com.autopartes.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCompatibilityResponse {
    private Long id;
    private Long productId;
    private String productSku;
    private String productNombre;
    private Long vehicleModelId;
    private String modelNombre;
    private String makeNombre;
    private Integer anioInicio;
    private Integer anioFin;
    private String motor;
    private String notas;
}
