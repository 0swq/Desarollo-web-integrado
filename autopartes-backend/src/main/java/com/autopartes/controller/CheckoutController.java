package com.autopartes.controller;
import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.order.*;
import com.autopartes.dto.pago.PagoResponse;
import com.autopartes.dto.itemorden.ItemOrdenResponse;
import com.autopartes.model.*;
import com.autopartes.service.*;
import com.autopartes.repository.OrdenRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.util.*;
import com.autopartes.exception.*;
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
 private final UsuarioService usuarios; private final OrdenService ordenes; private final PagoService pagos; private final ItemOrdenService items; private final OrdenRepository repository;
 public CheckoutController(UsuarioService usuarios, OrdenService ordenes, PagoService pagos, ItemOrdenService items, OrdenRepository repository) { this.usuarios=usuarios;this.ordenes=ordenes;this.pagos=pagos;this.items=items;this.repository=repository; }
 public record Datos(@NotBlank @Size(max=255) String direccionEntrega, @NotBlank @Size(max=20) @Pattern(regexp="[0-9+() -]+") String telefonoContacto, @Size(max=500) String notas) {}
 public record Simulacion(@NotNull Resultado resultado) {}
 public enum Resultado { APROBADO, RECHAZADO }
 public record Resumen(OrdenResponse orden, PagoResponse pago, List<ItemOrdenResponse> items) {}
 private UUID usuario(Principal principal) { return usuarios.buscarPorCorreo(principal.getName()).filter(u -> Boolean.TRUE.equals(u.getActivo())).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")).getId(); }
 private Orden propia(UUID id, UUID usuario) { return ordenes.buscarPorId(id).filter(o -> usuario.equals(o.getUsuarioId())).orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado")); }
 @PostMapping("/ordenes") @Transactional
 public ApiResponse<Resumen> crear(Principal principal, @Valid @RequestBody Datos datos) {
  var request=OrdenRequest.builder().direccionEntrega(datos.direccionEntrega()).telefonoContacto(datos.telefonoContacto()).notas(datos.notas()).metodoPago("SIMULADO").build();
  return ApiResponse.ok("Pedido de demostración creado", resumen(ordenes.crearOrdenDesdeCarrito(usuario(principal),request)));
 }
 @GetMapping("/ordenes/{id}") @Transactional(readOnly=true)
 public ApiResponse<Resumen> obtener(Principal principal,@PathVariable UUID id) { return ApiResponse.ok(resumen(propia(id,usuario(principal)))); }
 @PostMapping("/ordenes/{id}/simular") @Transactional
 public ApiResponse<Resumen> simular(Principal principal,@PathVariable UUID id,@Valid @RequestBody Simulacion request) {
  UUID usuario=usuario(principal);
  var orden=repository.bloquearPorId(id).filter(o -> usuario.equals(o.getUsuarioId())).orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
  var pago=pagos.buscarPorOrden(id).orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
  if (!"SIMULADO".equals(pago.getMetodoPago())) throw new BusinessException("Este pago no pertenece al simulador");
  if (pago.getEstado()==EstadoPago.APROBADO) return ApiResponse.ok(resumen(orden));
  if (orden.getEstado()!=EstadoOrden.PENDIENTE) throw new BusinessException("El pedido ya no admite intentos de pago");
  if (request.resultado()==Resultado.APROBADO) {
   pagos.actualizarEstado(pago.getId(),EstadoPago.APROBADO);
   orden=ordenes.actualizarEstado(id,EstadoOrden.PAGADO);
   return ApiResponse.ok("Pago aprobado",resumen(orden));
  }
  else pagos.actualizarEstado(pago.getId(),EstadoPago.RECHAZADO);
  return ApiResponse.ok("Resultado simulado; no se realizó un cobro",resumen(orden));
 }
 @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
 public org.springframework.http.ResponseEntity<ApiResponse<Void>> datosInvalidos() { return org.springframework.http.ResponseEntity.badRequest().body(ApiResponse.error("Datos de simulación inválidos")); }
 private Resumen resumen(Orden o) {
  var orden=OrdenResponse.builder().id(o.getId()).numeroOrden(o.getNumeroOrden()).usuarioId(o.getUsuarioId()).estado(o.getEstado()).subtotal(o.getSubtotal()).igv(o.getIgv()).total(o.getTotal()).moneda(o.getMoneda()).direccionEntrega(o.getDireccionEntrega()).telefonoContacto(o.getTelefonoContacto()).notas(o.getNotas()).fechaCreacion(o.getFechaCreacion()).fechaActualizacion(o.getFechaActualizacion()).build();
  var p=pagos.buscarPorOrden(o.getId()).orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
  var pago=PagoResponse.builder().id(p.getId()).ordenId(o.getId()).monto(p.getMonto()).moneda(p.getMoneda()).estado(p.getEstado()).metodoPago(p.getMetodoPago()).fechaCreacion(p.getFechaCreacion()).build();
  return new Resumen(orden,pago,lineas(o));
 }
 private List<ItemOrdenResponse> lineas(Orden o) { return items.buscarPorOrden(o.getId()).stream().map(i -> ItemOrdenResponse.builder().id(i.getId()).ordenId(o.getId()).productoId(i.getProductoId()).nombreProducto(i.getNombreProducto()).sku(i.getSku()).cantidad(i.getCantidad()).precioUnitario(i.getPrecioUnitario()).subtotal(i.getSubtotal()).build()).toList(); }
}
