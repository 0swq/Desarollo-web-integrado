package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "movimiento_stock")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MovimientoStock {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior")
    private Integer stockAnterior;

    @Column(name = "stock_nuevo")
    private Integer stockNuevo;

    private String motivo;

    private String referencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

@CreationTimestamp
    @Column(name = "fecha")
    private LocalDateTime fecha;

    public UUID getProductoId() {
        return producto != null ? producto.getId() : null;
    }

    public UUID getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
    }
}