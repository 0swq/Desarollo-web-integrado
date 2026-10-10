package com.autopartes.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "producto_categoria")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductoCategoria {
    @EmbeddedId
    private ProductoCategoriaId id;

    public UUID getProductoId() {
        return id != null ? id.getProductoId() : null;
    }

    public UUID getCategoriaId() {
        return id != null ? id.getCategoriaId() : null;
    }

    public void setProductoId(UUID productoId) {
        UUID categoriaIdActual = getCategoriaId();
        this.id = new ProductoCategoriaId(productoId, categoriaIdActual);
    }

    public void setCategoriaId(UUID categoriaId) {
        UUID productoIdActual = getProductoId();
        this.id = new ProductoCategoriaId(productoIdActual, categoriaId);
    }
}