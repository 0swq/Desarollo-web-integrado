package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "pago")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(length = 3)
    private String moneda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPago estado;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "mercado_pago_pago_id")
    private String mercadoPagoPagoId;

    @Column(name = "mercado_pago_preferencia_id")
    private String mercadoPagoPreferenciaId;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    public UUID getOrdenId() {
        return orden != null ? orden.getId() : null;
    }
}