package com.autopartes.controller;
import com.autopartes.config.SecurityConfig;
import com.autopartes.filter.JwtAuthFilter;
import com.autopartes.service.*;
import com.autopartes.storage.SupabaseStorageService;
import com.autopartes.model.*;
import com.autopartes.dto.auth.PerfilRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(PerfilController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class PerfilControllerTest {
 @Autowired MockMvc mvc;
 @MockBean UsuarioService usuarios;
 @MockBean OrdenService ordenes;
 @MockBean ItemOrdenService items;
 @MockBean SupabaseStorageService storage;
 @MockBean UserDetailsService users;
 @MockBean JwtService jwt;
 private Usuario usuario() { var u=new Usuario(); u.setId(UUID.randomUUID()); u.setCorreo("ana@example.com"); u.setNombre("Ana"); u.setApellido("Perez"); u.setActivo(true); u.setRol(Rol.CLIENTE); return u; }
 @Test void anonimoNoPuedeConsultar() throws Exception { mvc.perform(get("/api/perfil")).andExpect(status().isUnauthorized()); verifyNoInteractions(usuarios); }
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void clienteObtieneSoloSuPerfil() throws Exception {
  var u=usuario(); when(usuarios.buscarPorCorreo("ana@example.com")).thenReturn(Optional.of(u));
  mvc.perform(get("/api/perfil")).andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(u.getId().toString()));
 }
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void clienteActualizaSinContrasenaNiRol() throws Exception {
  var u=usuario(); when(usuarios.buscarPorCorreo(anyString())).thenReturn(Optional.of(u)); when(usuarios.actualizarPerfil(eq(u.getId()),any())).thenReturn(u);
  mvc.perform(put("/api/perfil").contentType("application/json").content("{\"nombre\":\"Ana\",\"apellido\":\"Perez\",\"telefono\":\"940061944\",\"direccion\":\"Chimbote\",\"fotoPerfilUrl\":\"\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.rol").value("CLIENTE"));
  verify(usuarios).actualizarPerfil(eq(u.getId()),any(PerfilRequest.class));
 }
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void rechazaDatosInvalidos() throws Exception {
  mvc.perform(put("/api/perfil").contentType("application/json").content("""
    {"nombre":" ","apellido":"Perez","fotoPerfilUrl":"javascript:alert(1)"}
    """)).andExpect(status().isBadRequest());
  verify(usuarios,never()).actualizarPerfil(any(),any());
 }
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void noExponeItemsDeOtroCliente() throws Exception {
  var u=usuario(); when(usuarios.buscarPorCorreo(anyString())).thenReturn(Optional.of(u));
  var orden=new Orden(); orden.setId(UUID.randomUUID()); orden.setUsuario(usuario()); when(ordenes.buscarPorId(orden.getId())).thenReturn(Optional.of(orden));
  mvc.perform(get("/api/perfil/pedidos/"+orden.getId()+"/items")).andExpect(status().isNotFound()); verifyNoInteractions(items);
 }
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void permiteDetalleDelPedidoPropio() throws Exception {
  var u=usuario(); when(usuarios.buscarPorCorreo(anyString())).thenReturn(Optional.of(u)); var orden=new Orden(); orden.setId(UUID.randomUUID()); orden.setUsuario(u);
  when(ordenes.buscarPorId(orden.getId())).thenReturn(Optional.of(orden)); when(items.buscarPorOrden(orden.getId())).thenReturn(List.of());
  mvc.perform(get("/api/perfil/pedidos/"+orden.getId()+"/items")).andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
 }

 @Test void anonimoNoPuedeCambiarCredenciales() throws Exception {
  mvc.perform(patch("/api/perfil/password").contentType("application/json").content("{}" )).andExpect(status().isUnauthorized());
  mvc.perform(patch("/api/perfil/correo").contentType("application/json").content("{}" )).andExpect(status().isUnauthorized());
 }
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void passwordSeCambiaSoloParaElUsuarioActual() throws Exception {
  var u=usuario(); when(usuarios.buscarPorCorreo(anyString())).thenReturn(Optional.of(u));
  mvc.perform(patch("/api/perfil/password").contentType("application/json").content("""
   {"contrasenaActual":"Anterior123","contrasenaNueva":"Nueva123456","confirmacionContrasena":"Nueva123456","id":"otro"}
   """)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
  verify(usuarios).cambiarPasswordPropio(eq(u.getId()),any());
 }
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void rechazaPasswordDemasiadoCorta() throws Exception {
  mvc.perform(patch("/api/perfil/password").contentType("application/json").content("""
   {"contrasenaActual":"Anterior123","contrasenaNueva":"123","confirmacionContrasena":"123"}
   """)).andExpect(status().isBadRequest());
  verify(usuarios,never()).cambiarPasswordPropio(any(),any());
 }
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void correoDevuelveSesionNueva() throws Exception {
  var u=usuario(); when(usuarios.buscarPorCorreo(anyString())).thenReturn(Optional.of(u));
  u.setCorreo("nuevo@example.com"); when(usuarios.cambiarCorreoPropio(eq(u.getId()),any())).thenReturn(u); when(jwt.generateToken("nuevo@example.com")).thenReturn("token-renovado");
  mvc.perform(patch("/api/perfil/correo").contentType("application/json").content("""
   {"correoNuevo":"nuevo@example.com","confirmacionCorreo":"nuevo@example.com","contrasenaActual":"Anterior123"}
   """)).andExpect(status().isOk()).andExpect(jsonPath("$.data.correo").value("nuevo@example.com"))
   .andExpect(jsonPath("$.data.token").value("token-renovado")).andExpect(jsonPath("$.data.tokenType").value("Bearer"));
 }
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void rechazaCorreoInvalido() throws Exception {
  mvc.perform(patch("/api/perfil/correo").contentType("application/json").content("""
   {"correoNuevo":"no-es-correo","confirmacionCorreo":"no-es-correo","contrasenaActual":"Anterior123"}
   """)).andExpect(status().isBadRequest());
  verify(usuarios,never()).cambiarCorreoPropio(any(),any());
 }
}