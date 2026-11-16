package com.autopartes.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.Reader;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@EnabledIfSystemProperty(named = "storage.live", matches = "true")
class SupabaseStorageLiveTest {
    @Test void cicloRealConImagenTemporal() throws Exception {
        Properties env = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of("../.env"), StandardCharsets.UTF_8)) { env.load(reader); }
        var config = new SupabaseStorageProperties();
        config.setUrl(env.getProperty("SUPABASE_URL", ""));
        config.setKey(env.getProperty("SUPABASE_STORAGE_KEY", ""));
        config.setBucket(env.getProperty("SUPABASE_STORAGE_BUCKET", "autopartes-imagenes"));
        var service = new SupabaseStorageService(config, new ObjectMapper());
        byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Zl1sAAAAASUVORK5CYII=");
        var file = new MockMultipartFile("archivo", "temporal.png", "image/png", png);
        var image = service.crear(file, "productos");
        try {
            var response = new MockHttpServletResponse();
            service.transmitir(image.ruta(), null, null, response);
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getContentAsByteArray()).isEqualTo(png);
            service.sobrescribir(image.ruta(), file);
            var partial = new MockHttpServletResponse();
            service.transmitir(image.ruta(), "bytes=0-7", null, partial);
            assertThat(partial.getStatus()).isEqualTo(206);
            assertThat(partial.getContentAsByteArray()).isEqualTo(Arrays.copyOf(png, 8));
            var cached = new MockHttpServletResponse();
            service.transmitir(image.ruta(), null, response.getHeader("ETag"), cached);
            assertThat(cached.getStatus()).isIn(200, 304);
        } finally { service.eliminar(image.ruta()); }
        assertThatThrownBy(() -> service.transmitir(image.ruta(), null, null, new MockHttpServletResponse()))
                .isInstanceOfSatisfying(StorageException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
