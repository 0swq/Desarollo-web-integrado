package com.autopartes.controller;
import com.autopartes.config.SecurityConfig;
import com.autopartes.filter.JwtAuthFilter;
import com.autopartes.service.*;
import com.autopartes.repository.OrdenRepository;
import com.autopartes.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(CheckoutController.class)
@Import({SecurityConfig.class,JwtAuthFilter.class})
class CheckoutControllerTest {
 @Autowired MockMvc mvc;
 @MockBean UsuarioService usuarios; @MockBean OrdenService ordenes; @MockBean PagoService pagos; @MockBean ItemOrdenService items; @MockBean OrdenRepository repository;
 @MockBean UserDetailsService users; @MockBean JwtService jwt;
 Usuario usuario; Orden orden; Pago pago;
 @BeforeEach void datos(){
  usuario=new Usuario();usuario.setId(UUID.randomUUID());usuario.setActivo(true);usuario.setCorreo("ana@example.com");
  orden=new Orden();orden.setId(UUID.randomUUID());orden.setUsuario(usuario);orden.setNumeroOrden("ORD-DEMO");orden.setEstado(EstadoOrden.PENDIENTE);orden.setTotal(new BigDecimal("118.00"));
  pago=new Pago();pago.setId(UUID.randomUUID());pago.setOrden(orden);pago.setEstado(EstadoPago.PENDIENTE);pago.setMetodoPago("SIMULADO");pago.setMonto(orden.getTotal());pago.setMoneda("PEN");
  when(usuarios.buscarPorCorreo(anyString())).thenReturn(Optional.of(usuario));when(ordenes.buscarPorId(orden.getId())).thenReturn(Optional.of(orden));when(repository.bloquearPorId(orden.getId())).thenReturn(Optional.of(orden));when(pagos.buscarPorOrden(orden.getId())).thenReturn(Optional.of(pago));when(items.buscarPorOrden(orden.getId())).thenReturn(List.of());
  when(pagos.actualizarEstado(any(),any())).thenAnswer(i->{pago.setEstado(i.getArgument(1));return pago;});
  when(ordenes.actualizarEstado(any(),any())).thenAnswer(i->{orden.setEstado(i.getArgument(1));return orden;});
 }
 @Test void anonimoNoPuedeCrearNiSimular() throws Exception {mvc.perform(post("/api/checkout/ordenes").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());mvc.perform(post("/api/checkout/ordenes/"+orden.getId()+"/simular").contentType("application/json").content("{}")).andExpect(status().isUnauthorized());}
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void noLeePedidoAjeno() throws Exception {orden.setUsuario(new Usuario());mvc.perform(get("/api/checkout/ordenes/"+orden.getId())).andExpect(status().isNotFound());verifyNoInteractions(pagos);}
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void noSimulaUnPagoReal() throws Exception {pago.setMetodoPago("MERCADO_PAGO");mvc.perform(post("/api/checkout/ordenes/"+orden.getId()+"/simular").contentType("application/json").content("{\"resultado\":\"APROBADO\"}")).andExpect(status().isBadRequest());verify(pagos,never()).actualizarEstado(any(),any());}
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void apruebaSoloElPedidoPropioDeSimulacion() throws Exception {mvc.perform(post("/api/checkout/ordenes/"+orden.getId()+"/simular").contentType("application/json").content("{\"resultado\":\"APROBADO\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.data.pago.estado").value("APROBADO")).andExpect(jsonPath("$.data.orden.estado").value("PAGADO"));}
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void rechazadoPermanecePendienteYPermiteReintentar() throws Exception {mvc.perform(post("/api/checkout/ordenes/"+orden.getId()+"/simular").contentType("application/json").content("{\"resultado\":\"RECHAZADO\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.data.pago.estado").value("RECHAZADO")).andExpect(jsonPath("$.data.orden.estado").value("PENDIENTE"));verify(ordenes,never()).actualizarEstado(any(),any());}
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void aprobacionRepetidaNoRepiteCambios() throws Exception {pago.setEstado(EstadoPago.APROBADO);orden.setEstado(EstadoOrden.PAGADO);mvc.perform(post("/api/checkout/ordenes/"+orden.getId()+"/simular").contentType("application/json").content("{\"resultado\":\"APROBADO\"}")).andExpect(status().isOk());verify(pagos,never()).actualizarEstado(any(),any());}
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void crearImponeMetodoSimuladoYUsuarioActual() throws Exception {when(ordenes.crearOrdenDesdeCarrito(eq(usuario.getId()),any())).thenReturn(orden);mvc.perform(post("/api/checkout/ordenes").contentType("application/json").content("""
 {"direccionEntrega":"Chimbote, calle de prueba 123","telefonoContacto":"940061944","notas":"Prueba","metodoPago":"MERCADO_PAGO","usuarioId":"ajeno"}
 """)).andExpect(status().isOk()).andExpect(jsonPath("$.data.orden.numeroOrden").value("ORD-DEMO"));verify(ordenes).crearOrdenDesdeCarrito(eq(usuario.getId()),argThat(r->"SIMULADO".equals(r.getMetodoPago())));}
 @Test @WithMockUser(username="ana@example.com",roles="CLIENTE") void resultadoInvalidoEsErrorDeCliente() throws Exception {mvc.perform(post("/api/checkout/ordenes/"+orden.getId()+"/simular").contentType("application/json").content("{\"resultado\":\"FALSO\"}")).andExpect(status().isBadRequest());}
}
