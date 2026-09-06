package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_makes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleMake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(length = 60)
    private String paisOrigen;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
