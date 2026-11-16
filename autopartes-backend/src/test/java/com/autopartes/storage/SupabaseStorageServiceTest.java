package com.autopartes.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.*;

class SupabaseStorageServiceTest {
    private HttpServer server;
    private SupabaseStorageProperties config;
    private SupabaseStorageService service;
    private final List<Request> requests = new CopyOnWriteArrayList<>();
    private volatile int status = 200;
    private volatile byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
    private volatile String contentType = "application/json";
    private volatile boolean missingBucket;
    private static final byte[] PNG = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9Zl1sAAAAASUVORK5CYII=");
    record Request(String method, String path, String key, String auth, String range, String condition, byte[] body) {}

    @BeforeEach void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requests.add(new Request(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
                    exchange.getRequestHeaders().getFirst("apikey"), exchange.getRequestHeaders().getFirst("Authorization"),
                    exchange.getRequestHeaders().getFirst("Range"), exchange.getRequestHeaders().getFirst("If-None-Match"),
                    exchange.getRequestBody().readAllBytes()));
            boolean bucketMissing = missingBucket && exchange.getRequestMethod().equals("GET") && exchange.getRequestURI().getPath().contains("/bucket/");
            byte[] data = bucketMissing ? "{\"statusCode\":\"404\",\"message\":\"Bucket not found\"}".getBytes(StandardCharsets.UTF_8) : body;
            int code = bucketMissing ? 400 : status;
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("ETag", "\"imagen-v1\"");
            if (code == 206) exchange.getResponseHeaders().set("Content-Range", "bytes 0-3/68");
            exchange.sendResponseHeaders(code, code == 304 ? -1 : data.length);
            if (code != 304) exchange.getResponseBody().write(data);
            exchange.close();
        });
        server.start();
        config = new SupabaseStorageProperties();
        config.setUrl("http://127.0.0.1:" + server.getAddress().getPort());
        config.setKey("sb_secret_test-only");
        config.setCreateBucket(false);
        service = new SupabaseStorageService(config, new ObjectMapper());
    }
    @AfterEach void tearDown() { server.stop(0); }
    private MockMultipartFile image() {
        return new MockMultipartFile("archivo", "nombre-no-confiable.html", "image/png", PNG) {
            @Override public byte[] getBytes() { throw new AssertionError("No cargar el archivo completo en memoria"); }
        };
    }
    @Test void creaConNombreUnicoYTransmiteSinGetBytes() {
        StorageImageResponse image = service.crear(image(), "productos");
        assertThat(image.ruta()).matches("productos/[a-f0-9-]+\\.png");
        assertThat(image.url()).isEqualTo("/api/storage/imagenes/" + image.ruta());
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).method()).isEqualTo("POST");
        assertThat(requests.get(0).key()).isEqualTo("sb_secret_test-only");
        assertThat(requests.get(0).auth()).isNull();
        assertThat(requests.get(0).body()).isEqualTo(PNG);
    }
    @Test void creaBucketPrivadoSiNoExiste() throws Exception {
        config.setCreateBucket(true); missingBucket = true;
        service.crear(image(), "categorias");
        assertThat(requests).hasSize(3);
        var bucket = new ObjectMapper().readTree(requests.get(1).body());
        assertThat(bucket.path("public").asBoolean()).isFalse();
        assertThat(bucket.path("allowed_mime_types").size()).isEqualTo(4);
        assertThat(requests.get(2).path()).contains("/object/autopartes-imagenes/categorias/");
    }
    @Test void reemplazaConPutYEliminaConDelete() {
        service.sobrescribir("/productos/prueba.png", image());
        service.eliminar("productos/prueba.png");
        assertThat(requests.stream().map(Request::method)).containsExactly("PUT", "DELETE");
    }
    @Test void transmiteImagenYMetadatos() throws Exception {
        body = PNG; contentType = "image/png";
        var response = new MockHttpServletResponse();
        service.transmitir("productos/prueba.png", null, null, response);
        assertThat(response.getContentAsByteArray()).isEqualTo(PNG);
        assertThat(response.getContentType()).isEqualTo("image/png");
        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(requests.get(0).path()).contains("/object/authenticated/");
    }
    @Test void respetaRangosYCopiaContentRange() throws Exception {
        status = 206; body = Arrays.copyOf(PNG, 4); contentType = "image/png";
        var response = new MockHttpServletResponse();
        service.transmitir("productos/prueba.png", "bytes=0-3", null, response);
        assertThat(response.getStatus()).isEqualTo(206);
        assertThat(response.getHeader("Content-Range")).isEqualTo("bytes 0-3/68");
        assertThat(requests.get(0).range()).isEqualTo("bytes=0-3");
        assertThat(response.getContentAsByteArray()).hasSize(4);
    }
    @Test void devuelve304SinCuerpo() throws Exception {
        status = 304;
        var response = new MockHttpServletResponse();
        service.transmitir("productos/prueba.png", null, "\"imagen-v1\"", response);
        assertThat(response.getStatus()).isEqualTo(304);
        assertThat(response.getContentAsByteArray()).isEmpty();
        assertThat(requests.get(0).condition()).isEqualTo("\"imagen-v1\"");
    }
    @Test void rechazaRutasYFormatosPeligrososAntesDeLlamarSupabase() {
        assertThatThrownBy(() -> service.eliminar("productos/../privado.png")).isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> service.eliminar("https://externo/prueba.png")).isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> service.crear(new MockMultipartFile("archivo", "x.png", "image/png", "<script>no es imagen</script>".getBytes()), "productos"))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> service.sobrescribir("productos/prueba.jpg", image())).isInstanceOf(StorageException.class);
        assertThat(requests).isEmpty();
    }
    @Test void rechazaTamanioYClaveIncorrectos() {
        config.setMaxBytes(1);
        assertThatThrownBy(() -> service.crear(image(), "productos")).isInstanceOfSatisfying(StorageException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE));
        config.setKey("");
        assertThatThrownBy(() -> service.eliminar("productos/a.png")).isInstanceOfSatisfying(StorageException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        assertThat(requests).isEmpty();
    }
    @Test void mapeaErrorDelProveedorSinFiltrarSuRespuesta() {
        status = 400; body = "{\"statusCode\":\"404\",\"code\":\"NoSuchKey\",\"message\":\"dato-privado\"}".getBytes();
        assertThatThrownBy(() -> service.eliminar("productos/a.png")).isInstanceOfSatisfying(StorageException.class, ex -> {
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(ex.getMessage()).doesNotContain("dato-privado");
        });
    }
    @Test void soportaAutenticacionLegacySoloEnServidor() {
        config.setKey("eyJ.test-service-role.jwt");
        service.eliminar("productos/prueba.png");
        assertThat(requests.get(0).auth()).isEqualTo("Bearer eyJ.test-service-role.jwt");
    }
}
