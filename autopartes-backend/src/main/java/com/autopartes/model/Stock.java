package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "ubicacion_almacen", length = 100)
    private String ubicacionAlmacen;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public UUID getProductoId() {
        return producto != null ? producto.getId() : null;
    }
}