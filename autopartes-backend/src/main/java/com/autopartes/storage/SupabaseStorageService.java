package com.autopartes.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class SupabaseStorageService {
    private static final Map<String, String> TYPES = Map.of(
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp", "gif", "image/gif");
    private static final Pattern PATH = Pattern.compile("(?:productos|categorias|modelos|marcas|perfiles)/[a-zA-Z0-9_-]+\\.(?:jpg|jpeg|png|webp|gif)");
    private final SupabaseStorageProperties config;
    private final ObjectMapper mapper;
    private volatile boolean bucketReady;

    public SupabaseStorageService(SupabaseStorageProperties config, ObjectMapper mapper) {
        this.config = config;
        this.mapper = mapper;
    }

    public StorageImageResponse crear(MultipartFile archivo, String carpeta) {
        if (!Set.of("productos", "categorias", "modelos", "marcas", "perfiles").contains(carpeta))
            throw new StorageException(HttpStatus.BAD_REQUEST, "La carpeta debe ser productos, categorias, modelos, marcas o perfiles.");
        String type = validarImagen(archivo);
        String extension = type.equals("image/jpeg") ? "jpg" : type.substring("image/".length());
        String ruta = carpeta + "/" + UUID.randomUUID() + "." + extension;
        guardar("POST", ruta, archivo, type);
        return resultado(ruta, type, archivo.getSize());
    }

    /** PUT tiene semántica upsert: conserva la ruta y crea el objeto si no existía. */
    public StorageImageResponse sobrescribir(String ruta, MultipartFile archivo) {
        ruta = validarRuta(ruta);
        String type = validarImagen(archivo);
        if (!type.equals(TYPES.get(ruta.substring(ruta.lastIndexOf('.') + 1))))
            throw new StorageException(HttpStatus.BAD_REQUEST, "La imagen debe conservar el formato de la ruta original.");
        guardar("PUT", ruta, archivo, type);
        return resultado(ruta, type, archivo.getSize());
    }

    public void eliminar(String ruta) {
        ruta = validarRuta(ruta);
        HttpURLConnection con = abrir("DELETE", "/object/" + config.getBucket() + "/" + ruta);
        try { comprobar(con); }
        catch (IOException e) { throw conexion(); }
        finally { cerrar(con); }
    }

    /** Copia el stream remoto directamente al cliente; no carga la imagen completa en memoria. */
    public void transmitir(String ruta, String range, String ifNoneMatch, HttpServletResponse response) throws IOException {
        ruta = validarRuta(ruta);
        if (range != null && !range.matches("bytes=(?:[0-9]{1,18}-[0-9]{0,18}|-[0-9]{1,18})"))
            throw new StorageException(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "Se admite un único rango de bytes.");
        HttpURLConnection con = abrir("GET", "/object/authenticated/" + config.getBucket() + "/" + ruta);
        if (range != null) con.setRequestProperty("Range", range);
        if (ifNoneMatch != null && ifNoneMatch.length() <= 512 && !ifNoneMatch.contains("\r") && !ifNoneMatch.contains("\n"))
            con.setRequestProperty("If-None-Match", ifNoneMatch);
        try {
            int status;
            try { status = comprobar(con); }
            catch (IOException e) { throw conexion(); }
            response.setHeader("Cache-Control", "public, no-cache");
            copiarHeader(con, response, "ETag");
            copiarHeader(con, response, "Last-Modified");
            if (status == 304) { response.setStatus(304); return; }
            String type = Optional.ofNullable(con.getContentType()).orElse("").split(";")[0].trim().toLowerCase(Locale.ROOT);
            if (!TYPES.containsValue(type))
                throw new StorageException(HttpStatus.BAD_GATEWAY, "El archivo remoto no es una imagen admitida.");
            long length = con.getContentLengthLong();
            if (length > config.getMaxBytes())
                throw new StorageException(HttpStatus.PAYLOAD_TOO_LARGE, "La imagen excede el tamaño permitido.");
            response.setStatus(status);
            response.setContentType(type);
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Content-Disposition", "inline; filename=\"" + ruta.substring(ruta.lastIndexOf('/') + 1) + "\"");
            copiarHeader(con, response, "Accept-Ranges");
            copiarHeader(con, response, "Content-Range");
            if (length >= 0) response.setContentLengthLong(length);
            try (InputStream in = con.getInputStream()) {
                byte[] buffer = new byte[16384];
                long total = 0;
                int read;
                while ((read = in.read(buffer)) != -1) {
                    total += read;
                    if (total > config.getMaxBytes()) throw new IOException("El archivo excede el límite de streaming.");
                    response.getOutputStream().write(buffer, 0, read);
                }
            }
        } finally { cerrar(con); }
    }

    private void guardar(String method, String ruta, MultipartFile archivo, String type) {
        asegurarBucket();
        HttpURLConnection con = abrir(method, "/object/" + config.getBucket() + "/" + ruta);
        con.setRequestProperty("Content-Type", type);
        con.setRequestProperty("Cache-Control", "0");
        if (method.equals("POST")) con.setRequestProperty("x-upsert", "false");
        con.setDoOutput(true);
        con.setFixedLengthStreamingMode(archivo.getSize());
        try {
            try (InputStream in = archivo.getInputStream(); OutputStream out = con.getOutputStream()) { in.transferTo(out); }
            comprobar(con);
        } catch (IOException e) { throw conexion(); }
        finally { cerrar(con); }
    }

    private synchronized void asegurarBucket() {
        if (bucketReady || !config.isCreateBucket()) return;
        HttpURLConnection con = abrir("GET", "/bucket/" + config.getBucket());
        try {
            int status = con.getResponseCode();
            // Supabase puede devolver HTTP 400 con statusCode 404 en su JSON.
            if (status >= 400) {
                StorageException error = errorRemoto(con, status);
                if (error.getStatus() != HttpStatus.NOT_FOUND) throw error;
            } else { bucketReady = true; return; }
        } catch (IOException e) { throw conexion(); }
        finally { cerrar(con); }
        con = abrir("POST", "/bucket");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");
        try {
            byte[] json = mapper.writeValueAsBytes(Map.of("id", config.getBucket(), "name", config.getBucket(),
                    "public", false, "file_size_limit", config.getMaxBytes(), "allowed_mime_types", List.of("image/jpeg", "image/png", "image/webp", "image/gif")));
            con.setFixedLengthStreamingMode(json.length);
            try (OutputStream out = con.getOutputStream()) { out.write(json); }
            try { comprobar(con); }
            catch (StorageException e) {
                if (e.getStatus() != HttpStatus.CONFLICT) throw e;
            }
            bucketReady = true;
        } catch (IOException e) { throw conexion(); }
        finally { cerrar(con); }
    }

    private String validarImagen(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new StorageException(HttpStatus.BAD_REQUEST, "Selecciona una imagen no vacía.");
        if (file.getSize() > config.getMaxBytes()) throw new StorageException(HttpStatus.PAYLOAD_TOO_LARGE, "La imagen excede el tamaño permitido.");
        byte[] b;
        try (InputStream in = file.getInputStream()) { b = in.readNBytes(12); }
        catch (IOException e) { throw new StorageException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen."); }
        String type = null;
        if (b.length >= 8 && Arrays.equals(Arrays.copyOf(b, 8), new byte[]{(byte)137,80,78,71,13,10,26,10})) type = "image/png";
        else if (b.length >= 3 && (b[0]&255)==255 && (b[1]&255)==216 && (b[2]&255)==255) type = "image/jpeg";
        else if (b.length >= 6 && (new String(b,0,6,StandardCharsets.US_ASCII).equals("GIF87a") || new String(b,0,6,StandardCharsets.US_ASCII).equals("GIF89a"))) type = "image/gif";
        else if (b.length == 12 && new String(b,0,4,StandardCharsets.US_ASCII).equals("RIFF") && new String(b,8,4,StandardCharsets.US_ASCII).equals("WEBP")) type = "image/webp";
        if (type == null) throw new StorageException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Solo se permiten imágenes JPEG, PNG, WebP y GIF.");
        String declarado = file.getContentType();
        if (declarado != null && !declarado.equals("application/octet-stream") && !declarado.equals(type))
            throw new StorageException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El tipo declarado no coincide con el contenido de la imagen.");
        return type;
    }

    private String validarRuta(String ruta) {
        if (ruta != null && ruta.startsWith("/")) ruta = ruta.substring(1);
        if (ruta == null || !PATH.matcher(ruta).matches())
            throw new StorageException(HttpStatus.BAD_REQUEST, "Ruta de imagen inválida. Utiliza la ruta devuelta al cargarla.");
        return ruta;
    }

    private HttpURLConnection abrir(String method, String path) {
        String base = config.getUrl().replaceAll("/+$", "");
        String key = config.getKey().trim();
        if (base.isBlank() || key.isBlank())
            throw new StorageException(HttpStatus.SERVICE_UNAVAILABLE, "Configura SUPABASE_URL y SUPABASE_STORAGE_KEY en el backend.");
        try {
            URI uri = URI.create(base);
            boolean local = Set.of("localhost", "127.0.0.1", "[::1]").contains(Optional.ofNullable(uri.getHost()).orElse(""));
            if (!("https".equals(uri.getScheme()) || (local && "http".equals(uri.getScheme()))) ||
                    uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null ||
                    (uri.getPath() != null && !uri.getPath().isEmpty()) || !config.getBucket().matches("[a-zA-Z0-9_-]{1,100}") ||
                    config.getMaxBytes() < 1 || config.getTimeoutMillis() < 1 || key.startsWith("sb_publishable_") || key.startsWith("sbp_"))
                throw new IllegalArgumentException();
            HttpURLConnection con = (HttpURLConnection) URI.create(base + "/storage/v1" + path).toURL().openConnection();
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(config.getTimeoutMillis());
            con.setReadTimeout(config.getTimeoutMillis());
            con.setRequestMethod(method);
            con.setRequestProperty("apikey", key);
            // Las secret keys nuevas no son JWT: el gateway autentica mediante apikey.
            if (!key.startsWith("sb_secret_")) con.setRequestProperty("Authorization", "Bearer " + key);
            return con;
        } catch (IllegalArgumentException e) {
            throw new StorageException(HttpStatus.SERVICE_UNAVAILABLE, "La configuración de Supabase Storage no es válida.");
        } catch (IOException e) { throw conexion(); }
    }

    private int comprobar(HttpURLConnection con) throws IOException {
        int status = con.getResponseCode();
        if ((status >= 200 && status < 300) || status == 304) return status;
        throw errorRemoto(con, status);
    }

    private StorageException errorRemoto(HttpURLConnection con, int status) {
        String code = "";
        try (InputStream stream = con.getErrorStream()) {
            if (stream != null) {
                JsonNode error = mapper.readTree(stream.readNBytes(8192));
                if (error == null) return conexion();
                code = error.path("code").asText("");
                status = error.path("statusCode").asInt(status);
                if (code.isEmpty() && error.path("message").asText("").equalsIgnoreCase("Bucket not found")) code = "NoSuchBucket";
            }
        } catch (IOException ignored) { /* No reenviar respuestas privadas del proveedor. */ }
        if (status == 404 || Set.of("NoSuchKey", "NoSuchBucket", "not_found").contains(code))
            return new StorageException(HttpStatus.NOT_FOUND, "No se encontró la imagen o el bucket configurado.");
        if (status == 409 || Set.of("Duplicate", "KeyAlreadyExists", "ResourceAlreadyExists").contains(code))
            return new StorageException(HttpStatus.CONFLICT, "Ya existe un archivo con esa ruta.");
        if (status == 413) return new StorageException(HttpStatus.PAYLOAD_TOO_LARGE, "Supabase rechazó el tamaño de la imagen.");
        if (status == 416) return new StorageException(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, "El rango solicitado no es válido.");
        if (status == 401 || status == 403)
            return new StorageException(HttpStatus.SERVICE_UNAVAILABLE, "Supabase rechazó la clave o los permisos de Storage. Revisa la configuración del backend.");
        return conexion();
    }

    private StorageException conexion() { return new StorageException(HttpStatus.BAD_GATEWAY, "No se pudo completar la operación con Supabase Storage."); }
    private StorageImageResponse resultado(String ruta, String type, long size) {
        return new StorageImageResponse(ruta, "/api/storage/imagenes/" + ruta, type, size);
    }
    private static void copiarHeader(HttpURLConnection con, HttpServletResponse response, String name) {
        String value = con.getHeaderField(name);
        if (value != null) response.setHeader(name, value);
    }
    private static void cerrar(HttpURLConnection con) {
        InputStream error = con.getErrorStream();
        if (error != null) try { error.close(); } catch (IOException ignored) { }
        con.disconnect();
    }
}
