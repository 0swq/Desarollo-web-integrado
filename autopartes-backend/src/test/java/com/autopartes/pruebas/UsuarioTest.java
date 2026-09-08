package com.autopartes.pruebas;

import com.autopartes.dto.auth.UsuarioRequest;
import com.autopartes.model.Rol;
import com.autopartes.model.Usuario;
import com.autopartes.repository.UsuarioRepository;
import com.autopartes.service.UsuarioService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class UsuarioTest {


    @Test
    public void probarRegistro() {
        UsuarioRepository repository = new UsuarioRepository();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        UsuarioService usuarioService = new UsuarioService(repository, passwordEncoder);
        UsuarioRequest registerRequest = new UsuarioRequest();
        registerRequest.setCorreo("hola@gmail.com");
        registerRequest.setContrasena("123");
        registerRequest.setNombre("uno");
        registerRequest.setApellido("dos");
        registerRequest.setTelefono("123456789");
        registerRequest.setDireccion("lugar");
        registerRequest.setRol(Rol.CLIENTE);

        Usuario usuarioResponse = usuarioService.registrar(registerRequest);
        Assertions.assertNotNull(usuarioResponse, "El usuario devuelto no debería ser nulo");
        Assertions.assertEquals("hola@gmail.com", usuarioResponse.getCorreo());
        Assertions.assertEquals("uno", usuarioResponse.getNombre());
    }

    @Test
    public void probarLogin() {
        UsuarioRepository repository = new UsuarioRepository();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        UsuarioService usuarioService = new UsuarioService(repository, passwordEncoder);

        UsuarioRequest registerRequest = new UsuarioRequest();
        registerRequest.setCorreo("login@gmail.com");
        registerRequest.setContrasena("123");
        registerRequest.setNombre("uno");
        registerRequest.setApellido("dos");
        registerRequest.setTelefono("123456789");
        registerRequest.setDireccion("lugar");
        registerRequest.setRol(Rol.CLIENTE);

        usuarioService.registrar(registerRequest);

        Optional<Usuario> usuarioOpt = usuarioService.validarCredenciales("login@gmail.com", "123");
        Assertions.assertTrue(usuarioOpt.isPresent(), "El usuario debería encontrarse con credenciales válidas");
        Assertions.assertEquals("login@gmail.com", usuarioOpt.get().getCorreo());
    }
}