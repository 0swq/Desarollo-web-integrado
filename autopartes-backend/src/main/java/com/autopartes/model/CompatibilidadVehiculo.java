package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compatibilidad_vehiculo")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CompatibilidadVehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modelo_vehiculo_id", nullable = false)
    private ModeloVehiculo modeloVehiculo;

    @Column(name = "anio_inicio")
    private Integer anioInicio;

    @Column(name = "anio_fin")
    private Integer anioFin;

    private String motor;

    @Column(length = 500)
    private String notas;

    public UUID getProductoId() {
        return producto != null ? producto.getId() : null;
    }

    public UUID getModeloVehiculoId() {
        return modeloVehiculo != null ? modeloVehiculo.getId() : null;
    }
}