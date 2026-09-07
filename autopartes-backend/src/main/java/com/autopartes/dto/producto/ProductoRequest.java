package com.autopartes.dto.producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoRequest {

    @NotBlank(message = "El código SKU es obligatorio")
    private String sku;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    private BigDecimal precioPromocional;
    private String imagenUrl;

    @NotNull(message = "La categoría es obligatoria")
    private UUID categoriaId;

    private UUID proveedorId;
    private Integer stockInicial;
    private Integer stockMinimo;
    private String ubicacionAlmacen;

    @Builder.Default
    private Boolean activo = true;
}
