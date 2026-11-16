package com.autopartes.controller;
import com.autopartes.config.SecurityConfig;
import com.autopartes.filter.JwtAuthFilter;
import com.autopartes.service.*;
import com.autopartes.model.ParametroTienda;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(ParametrosController.class) @Import({SecurityConfig.class,JwtAuthFilter.class})
class ParametrosControllerTest {
 @Autowired MockMvc mvc; @MockBean ParametrosService service; @MockBean UserDetailsService users; @MockBean JwtService jwt;
 private String datos(){return """
 {"nombre":"Tienda","local":"Local","telefono":"940 061 944","whatsapp":"51940061944","direccion":"Calle 123","correoContacto":"","horario":"","latitud":-9.1374281,"longitud":-78.5063972,"mapsUrl":"https://www.google.com/maps?q=-9,-78","version":0}
 """;}
 @Test void datosSonPublicos() throws Exception {var p=new ParametroTienda();p.setNombre("Tienda");when(service.obtener()).thenReturn(p);mvc.perform(get("/api/parametros")).andExpect(status().isOk()).andExpect(jsonPath("$.data.nombre").value("Tienda"));}
 @Test void anonimoNoGuarda() throws Exception {mvc.perform(put("/api/parametros").contentType("application/json").content(datos())).andExpect(status().isUnauthorized());verify(service,never()).actualizar(any());}
 @Test @WithMockUser(roles="EMPLEADO") void empleadoNoGuarda() throws Exception {mvc.perform(put("/api/parametros").contentType("application/json").content(datos())).andExpect(status().isForbidden());verify(service,never()).actualizar(any());}
 @Test @WithMockUser(roles="CLIENTE") void clienteNoGuarda() throws Exception {mvc.perform(put("/api/parametros").contentType("application/json").content(datos())).andExpect(status().isForbidden());}
 @Test @WithMockUser(roles="ADMIN") void adminActualiza() throws Exception {when(service.actualizar(any())).thenReturn(new ParametroTienda());mvc.perform(put("/api/parametros").contentType("application/json").content(datos())).andExpect(status().isOk());verify(service).actualizar(any());}
 @Test @WithMockUser(roles="ADMIN") void coordenadasInvalidasSonRechazadas() throws Exception {mvc.perform(put("/api/parametros").contentType("application/json").content(datos().replace("-9.1374281","120"))).andExpect(status().isBadRequest());verify(service,never()).actualizar(any());}
 @Test @WithMockUser(roles="ADMIN") void validaCadaCampoYDevuelveErroresEspecificos() throws Exception {
  var mapper=new com.fasterxml.jackson.databind.ObjectMapper();
  String[][] casos={{"nombre","A"},{"local"," "},{"telefono","---"},{"telefono","123"},{"whatsapp","+51940061944"},{"direccion","abc"},{"correoContacto","contacto@dominio"},{"horario","x"},{"mapsUrl","https://www.google.com/"},{"mapsUrl","https://ejemplo.com/maps"},{"latitud","-9.12345678"}};
  for(var caso:casos){var json=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.readTree(datos());json.put(caso[0],caso[1]);mvc.perform(put("/api/parametros").contentType("application/json").content(mapper.writeValueAsString(json))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.data."+caso[0]).exists());}
  verify(service,never()).actualizar(any());
 }
 @Test @WithMockUser(roles="ADMIN") void rechazaCoordenadasNoNumericas() throws Exception {
  mvc.perform(put("/api/parametros").contentType("application/json").content(datos().replace("-9.1374281","\"NaN\""))).andExpect(status().isBadRequest());verify(service,never()).actualizar(any());
 }
}
