package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_models")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "make_id", nullable = false)
    private VehicleMake make;

    @Column(length = 50)
    private String tipoVehiculo; // Sedan, SUV, Camioneta, Hatchback

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
