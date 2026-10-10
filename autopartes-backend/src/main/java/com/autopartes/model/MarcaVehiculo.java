package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "marca_vehiculo")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MarcaVehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "pais_origen", length = 100)
    private String paisOrigen;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}