package com.autopartes.storage;

import com.autopartes.config.SecurityConfig;
import com.autopartes.filter.JwtAuthFilter;
import com.autopartes.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StorageController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, StorageExceptionHandler.class})
class StorageControllerTest {
    @Autowired MockMvc mvc;
    @MockBean SupabaseStorageService storage;
    @MockBean UserDetailsService users;
    @MockBean JwtService jwt;
    private MockMultipartFile file() { return new MockMultipartFile("archivo", "x.png", "image/png", new byte[]{1,2,3}); }

    @Test void lecturaEsPublicaYUsaStreaming() throws Exception {
        doAnswer(call -> {
            var response = call.getArgument(3, jakarta.servlet.http.HttpServletResponse.class);
            response.setContentType("image/png");
            response.getOutputStream().write(new byte[]{1,2,3});
            return null;
        }).when(storage).transmitir(eq("/productos/test.png"), isNull(), isNull(), any());
        mvc.perform(get("/api/storage/imagenes/productos/test.png")).andExpect(status().isOk())
                .andExpect(content().contentType("image/png")).andExpect(content().bytes(new byte[]{1,2,3}));
    }
    @Test void escrituraAnonimaNoPermitida() throws Exception {
        mvc.perform(multipart("/api/storage/imagenes").file(file())).andExpect(status().isUnauthorized());
        verifyNoInteractions(storage);
    }
    @Test @WithMockUser(roles="CLIENTE") void clienteNoPuedeEscribirNiBorrar() throws Exception {
        mvc.perform(multipart("/api/storage/imagenes").file(file())).andExpect(status().isForbidden());
        mvc.perform(delete("/api/storage/imagenes/productos/test.png")).andExpect(status().isForbidden());
        verifyNoInteractions(storage);
    }
    @Test @WithMockUser(roles="EMPLEADO") void empleadoPuedeCrearYRecibeUrl() throws Exception {
        when(storage.crear(any(), eq("productos"))).thenReturn(new StorageImageResponse("productos/a.png", "/api/storage/imagenes/productos/a.png", "image/png", 3));
        mvc.perform(multipart("/api/storage/imagenes").file(file())).andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.url").value("/api/storage/imagenes/productos/a.png"));
    }
    @Test @WithMockUser(roles="ADMIN") void adminPuedeReemplazarYEliminar() throws Exception {
        mvc.perform(multipart("/api/storage/imagenes/productos/test.png").file(file())
                .with(req -> { req.setMethod("PUT"); return req; })).andExpect(status().isOk());
        verify(storage).sobrescribir(eq("/productos/test.png"), any());
        mvc.perform(delete("/api/storage/imagenes/productos/test.png")).andExpect(status().isOk());
        verify(storage).eliminar("/productos/test.png");
    }
    @Test void erroresMantienenFormatoApiResponse() throws Exception {
        doThrow(new StorageException(org.springframework.http.HttpStatus.NOT_FOUND, "Imagen no encontrada"))
                .when(storage).transmitir(anyString(), any(), any(), any());
        mvc.perform(get("/api/storage/imagenes/productos/no.png")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.message").value("Imagen no encontrada"));
    }
}
