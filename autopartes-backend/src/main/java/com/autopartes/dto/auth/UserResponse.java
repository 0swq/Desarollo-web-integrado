package com.autopartes.dto.auth;

import com.autopartes.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private String direccion;
    private Role rol;
    private Boolean activo;
    private LocalDateTime createdAt;
}
