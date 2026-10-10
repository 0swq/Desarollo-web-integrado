package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "item_orden")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ItemOrden {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    // Copias del producto al momento de la compra (histórico)
    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto;

    @Column(nullable = false, length = 50)
    private String sku;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    public UUID getOrdenId() {
        return orden != null ? orden.getId() : null;
    }

    public UUID getProductoId() {
        return producto != null ? producto.getId() : null;
    }
}