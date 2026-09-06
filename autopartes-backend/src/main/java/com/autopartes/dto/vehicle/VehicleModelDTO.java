package com.autopartes.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModelDTO {
    private Long id;
    private String nombre;
    private Long makeId;
    private String makeNombre;
    private String tipoVehiculo;
    private Boolean activo;
}
