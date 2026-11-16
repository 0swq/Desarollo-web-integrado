package com.autopartes.controller;
import com.autopartes.dto.ApiResponse;
import com.autopartes.service.DashboardService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.util.Map;
@RestController
@RequestMapping("/api/dashboard")
@Tag(name="Dashboard",description="Indicadores y reportes del negocio")
public class DashboardController {
 private final DashboardService service;
 public DashboardController(DashboardService service){this.service=service;}
 @GetMapping
 @Operation(summary="Consultar dashboard por rango de fechas")
 public ResponseEntity<ApiResponse<Map<String,Object>>> obtener(
 @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate desde,
 @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate hasta){return ResponseEntity.ok(ApiResponse.ok(service.obtener(desde,hasta)));}
}