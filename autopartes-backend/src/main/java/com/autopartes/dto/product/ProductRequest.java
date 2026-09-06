package com.autopartes.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

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
    private Long categoriaId;

    private Long proveedorId;

    private Integer stockInicial;
    private Integer stockMinimo;
    private String ubicacionAlmacen;
    private Boolean activo = true;
}
