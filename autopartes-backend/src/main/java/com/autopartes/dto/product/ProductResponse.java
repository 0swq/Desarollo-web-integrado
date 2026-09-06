package com.autopartes.dto.product;

import com.autopartes.dto.category.CategoryResponse;
import com.autopartes.dto.supplier.SupplierResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private BigDecimal precioPromocional;
    private String moneda;
    private String imagenUrl;
    private CategoryResponse categoria;
    private SupplierResponse proveedor;
    private Integer stockActual;
    private Integer stockMinimo;
    private Boolean bajoStock;
    private String ubicacionAlmacen;
    private Boolean activo;
    private LocalDateTime createdAt;
}
