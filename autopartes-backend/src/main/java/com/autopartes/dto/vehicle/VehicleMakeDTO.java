package com.autopartes.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleMakeDTO {
    private Long id;
    private String nombre;
    private String paisOrigen;
    private Boolean activo;
}
