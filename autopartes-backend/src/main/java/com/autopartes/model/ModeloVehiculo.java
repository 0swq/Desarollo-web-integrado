package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "modelo_vehiculo")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ModeloVehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marca_id", nullable = false)
    private MarcaVehiculo marca;

    @Column(name = "tipo_vehiculo", length = 50)
    private String tipoVehiculo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    public UUID getMarcaId() {
        return marca != null ? marca.getId() : null;
    }
}