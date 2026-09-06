package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_compatibilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_model_id", nullable = false)
    private VehicleModel vehicleModel;

    @Column(nullable = false)
    private Integer anioInicio;

    @Column(nullable = false)
    private Integer anioFin;

    @Column(length = 80)
    private String motor; // ej: 1.5L Dual VVT-i, 2.4L Diesel

    @Column(length = 250)
    private String notas; // ej: Compatible solo con versión manual o versión japonesa
}
