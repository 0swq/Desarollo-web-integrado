package com.autopartes.controller;
import com.autopartes.service.*;
import com.autopartes.model.*;
import com.autopartes.dto.auth.UsuarioRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class RegistroRolTest {
 @Test void elRegistroPublicoNoPermitePedirRolAdministrador(){var service=mock(UsuarioService.class);when(service.registrar(any())).thenReturn(new Usuario());var controller=new AuthController(service,mock(JwtService.class),mock(AuthenticationManager.class));var request=new UsuarioRequest();request.setRol(Rol.ADMIN);controller.register(request);verify(service).registrar(argThat(r->r.getRol()==Rol.CLIENTE));}
}
