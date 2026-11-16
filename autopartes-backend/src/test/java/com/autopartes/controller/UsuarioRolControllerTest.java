package com.autopartes.controller;
import com.autopartes.config.SecurityConfig;
import com.autopartes.filter.JwtAuthFilter;
import com.autopartes.service.*;
import com.autopartes.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(UsuarioController.class) @Import({SecurityConfig.class,JwtAuthFilter.class})
class UsuarioRolControllerTest {
 @Autowired MockMvc mvc; @MockBean UsuarioService usuarios; @MockBean JwtService jwt; @MockBean UserDetailsService users;
 private final UUID id=UUID.randomUUID();
 @Test void anonimoNoCambiaRol() throws Exception {mvc.perform(patch("/api/usuarios/"+id+"/rol").contentType("application/json").content("{\"rol\":\"ADMIN\"}")).andExpect(status().isUnauthorized());verifyNoInteractions(usuarios);}
 @Test @WithMockUser(roles="EMPLEADO") void empleadoNoCambiaRol() throws Exception {mvc.perform(patch("/api/usuarios/"+id+"/rol").contentType("application/json").content("{\"rol\":\"ADMIN\"}")).andExpect(status().isForbidden());verifyNoInteractions(usuarios);}
 @Test @WithMockUser(roles="CLIENTE") void clienteNoCambiaRol() throws Exception {mvc.perform(patch("/api/usuarios/"+id+"/rol").contentType("application/json").content("{\"rol\":\"ADMIN\"}")).andExpect(status().isForbidden());}
 @Test @WithMockUser(username="admin@example.com",roles="ADMIN") void adminCambiaSoloRol() throws Exception {var u=new Usuario();u.setId(id);u.setRol(Rol.EMPLEADO);when(usuarios.cambiarRol(id,Rol.EMPLEADO,"admin@example.com")).thenReturn(u);mvc.perform(patch("/api/usuarios/"+id+"/rol").contentType("application/json").content("{\"rol\":\"EMPLEADO\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.data.rol").value("EMPLEADO"));verify(usuarios).cambiarRol(id,Rol.EMPLEADO,"admin@example.com");}
 @Test @WithMockUser(roles="ADMIN") void requiereRol() throws Exception {mvc.perform(patch("/api/usuarios/"+id+"/rol").contentType("application/json").content("{}")).andExpect(status().isBadRequest());verifyNoInteractions(usuarios);}
 @Test @WithMockUser(roles="ADMIN") void rechazaRolDesconocido() throws Exception {mvc.perform(patch("/api/usuarios/"+id+"/rol").contentType("application/json").content("{\"rol\":\"SUPERADMIN\"}")).andExpect(status().isBadRequest());verifyNoInteractions(usuarios);}
}
