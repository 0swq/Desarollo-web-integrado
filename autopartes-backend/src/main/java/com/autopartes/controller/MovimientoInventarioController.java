package com.autopartes.controller;
import com.autopartes.dto.ApiResponse;
import com.autopartes.model.TipoMovimiento;
import com.autopartes.service.StockService;
import com.autopartes.repository.MovimientoStockRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
@RestController
@RequestMapping("/api/stocks/producto/{productoId}/movimientos")
@io.swagger.v3.oas.annotations.tags.Tag(name="Movimientos de stock")
public class MovimientoInventarioController {
 private final StockService stock; private final MovimientoStockRepository movimientos;
 public MovimientoInventarioController(StockService stock, MovimientoStockRepository movimientos){this.stock=stock;this.movimientos=movimientos;}
 public record Solicitud(@NotNull TipoMovimiento tipo,@NotNull @Min(0) Integer cantidad,@NotBlank @Size(min=5,max=255) String motivo,@Size(max=255) String referencia,@NotBlank String confirmacion,@NotNull @Min(0) Integer stockEsperado){}
 @GetMapping
 public ApiResponse<?> listar(@PathVariable UUID productoId){return ApiResponse.ok(movimientos.buscarPorProducto(productoId).stream().sorted(java.util.Comparator.comparing(com.autopartes.model.MovimientoStock::getFecha).reversed()).map(m->Map.of("id",m.getId(),"tipo",m.getTipo(),"cantidad",m.getCantidad(),"stockAnterior",m.getStockAnterior(),"stockNuevo",m.getStockNuevo(),"motivo",Objects.toString(m.getMotivo(),""),"referencia",Objects.toString(m.getReferencia(),""),"fecha",m.getFecha())).toList());}
 @PostMapping
 public ApiResponse<?> crear(@PathVariable UUID productoId,@Valid @RequestBody Solicitud r,java.security.Principal usuario){var m=stock.manual(productoId,r.tipo(),r.cantidad(),r.motivo(),r.referencia(),r.confirmacion(),r.stockEsperado(),usuario.getName());return ApiResponse.ok(Map.of("id",m.getId(),"stockNuevo",m.getStockNuevo()));}
}