package com.autopartes.storage;

import com.autopartes.dto.ApiResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice(assignableTypes = StorageController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StorageExceptionHandler {
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiResponse<Void>> storage(StorageException error) {
        return ResponseEntity.status(error.getStatus()).body(ApiResponse.error(error.getMessage()));
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> demasiadoGrande(MaxUploadSizeExceededException error) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ApiResponse.error("La imagen excede el tamaño permitido."));
    }
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> faltaArchivo(MissingServletRequestPartException error) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Envía la imagen en el campo multipart archivo."));
    }
}
