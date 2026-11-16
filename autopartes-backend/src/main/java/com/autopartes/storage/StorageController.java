package com.autopartes.storage;

import com.autopartes.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/storage/imagenes")
@Tag(name = "Storage", description = "Imágenes de productos y categorías en Supabase Storage")
public class StorageController {
    private final SupabaseStorageService storage;
    public StorageController(SupabaseStorageService storage) { this.storage = storage; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen", description = "ADMIN o EMPLEADO. Genera una ruta única y devuelve la URL de streaming para imagenUrl.")
    public ResponseEntity<ApiResponse<StorageImageResponse>> crear(
            @RequestPart("archivo") MultipartFile archivo,
            @RequestParam(defaultValue = "productos") String carpeta) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Imagen cargada", storage.crear(archivo, carpeta)));
    }

    @PutMapping(value = "/{*ruta}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Sobrescribir imagen", description = "ADMIN o EMPLEADO. Conserva la ruta y el formato; crea el archivo si no existía (upsert).")
    public ResponseEntity<ApiResponse<StorageImageResponse>> sobrescribir(
            @PathVariable String ruta, @RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(ApiResponse.ok("Imagen guardada", storage.sobrescribir(ruta, archivo)));
    }

    @DeleteMapping("/{*ruta}")
    @Operation(summary = "Eliminar imagen", description = "ADMIN o EMPLEADO. Elimina el archivo; no modifica registros de productos o categorías.")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable String ruta) {
        storage.eliminar(ruta);
        return ResponseEntity.ok(ApiResponse.ok("Imagen eliminada", null));
    }

    @GetMapping("/{*ruta}")
    @Operation(summary = "Obtener imagen por streaming", description = "Lectura pública del catálogo. Devuelve bytes de imagen, soporta un rango de bytes e If-None-Match; no devuelve JSON.")
    public void obtener(@PathVariable String ruta,
                        @RequestHeader(value = "Range", required = false) String range,
                        @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
                        HttpServletResponse response) throws IOException {
        storage.transmitir(ruta, range, ifNoneMatch, response);
    }
}
