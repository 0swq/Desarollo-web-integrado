package com.autopartes.dto.productocategoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoCategoriaResponse {

    private UUID productoId;
    private UUID categoriaId;
}
