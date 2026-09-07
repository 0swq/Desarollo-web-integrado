package com.autopartes.controller;

import com.autopartes.dto.ApiResponse;
import com.autopartes.dto.stock.StockRequest;
import com.autopartes.dto.stock.StockResponse;
import com.autopartes.model.Stock;
import com.autopartes.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockResponse>>> listar() {
        List<StockResponse> response = service.buscarTodos().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockResponse>> obtener(@PathVariable UUID id) {
        return service.buscarPorId(id)
                .map(s -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(s))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Stock no encontrado")));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<StockResponse>> porProducto(@PathVariable UUID productoId) {
        return service.buscarPorProducto(productoId)
                .map(s -> ResponseEntity.ok(ApiResponse.ok(mapToResponse(s))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Stock no encontrado")));
    }

    @GetMapping("/bajo-stock")
    public ResponseEntity<ApiResponse<List<StockResponse>>> bajoStock() {
        List<StockResponse> response = service.buscarBajoStock().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<StockResponse>> actualizarConfig(@PathVariable UUID productoId, @Valid @RequestBody StockRequest request) {
        Stock s = service.actualizarConfiguracion(productoId, request);
        return ResponseEntity.ok(ApiResponse.ok("Stock actualizado", mapToResponse(s)));
    }

    private StockResponse mapToResponse(Stock s) {
        StockResponse r = new StockResponse();
        r.setId(s.getId());
        r.setProductoId(s.getProductoId());
        r.setCantidad(s.getCantidad());
        r.setStockMinimo(s.getStockMinimo());
        r.setUbicacionAlmacen(s.getUbicacionAlmacen());
        r.setFechaActualizacion(s.getFechaActualizacion());
        return r;
    }
}