package com.autopartes.dto.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private Long id;
    private String ruc;
    private String razonSocial;
    private String contactoNombre;
    private String telefono;
    private String email;
    private String direccion;
    private Boolean activo;
    private LocalDateTime createdAt;
}
