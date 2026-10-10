package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class ProductoCategoriaId implements java.io.Serializable {
    @Column(name = "producto_id")
    private UUID productoId;

    @Column(name = "categoria_id")
    private UUID categoriaId;
}