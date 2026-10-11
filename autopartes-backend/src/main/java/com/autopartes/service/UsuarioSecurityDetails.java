package com.autopartes.service; // O el paquete exacto donde tengas tu clase

import com.autopartes.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UsuarioSecurityDetails implements UserDetails {

    private final Usuario usuario;

    public UsuarioSecurityDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convierte tu enum Rol (ej. ADMIN) al formato de Spring Security (ROLE_ADMIN)
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getContrasena(); // Apunta a tu campo de contraseña cifrada
    }

    @Override
    public String getUsername() {
        return usuario.getCorreo(); // Tu sistema usa el correo como identificador principal
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Opcional: aprovecha tu campo activo para bloquear usuarios si está en false
        return usuario.getActivo() != null ? usuario.getActivo() : true;
    }
}