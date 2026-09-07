package com.autopartes.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.autopartes.model.Rol;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthResponse {

    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private UUID id;
    private String correo;
    private String nombre;
    private String apellido;
    private Rol rol;
}