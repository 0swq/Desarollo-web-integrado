package com.autopartes.controller;
import com.autopartes.dto.*;
import com.autopartes.model.ParametroTienda;
import com.autopartes.service.ParametrosService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/parametros")
public class ParametrosController {
 private final ParametrosService service;
 public ParametrosController(ParametrosService service){this.service=service;}
 @GetMapping public ApiResponse<ParametroTienda> obtener(){return ApiResponse.ok(service.obtener());}
 @PutMapping public ApiResponse<ParametroTienda> guardar(@Valid @RequestBody ParametrosRequest request){return ApiResponse.ok("Parámetros actualizados",service.actualizar(request));}
 @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<ApiResponse<Void>> error(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(ApiResponse.error(e.getReason()));}
 @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class) public ResponseEntity<ApiResponse<Void>> conflicto(){return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("Los parámetros cambiaron. Recarga antes de guardar."));}
 @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class) public ResponseEntity<ApiResponse<Void>> formatoInvalido(){return ResponseEntity.badRequest().body(ApiResponse.error("Revisa el formato de los campos: las coordenadas deben ser números válidos."));}
}
