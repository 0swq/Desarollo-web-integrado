import com.autopartes.storage.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class CargarImagenesProductos {
    static final String[] SKUS = {"DEMO-ACE-5W30-1L", "DEMO-REF-5050-1L", "DEMO-LUZ-H4-12V", "DEMO-PLU-20IN", "DEMO-FIL-ACE-001"};
    static class Archivo implements MultipartFile {
        private final Path path;
        Archivo(Path path) { this.path = path; }
        public String getName() { return "archivo"; }
        public String getOriginalFilename() { return path.getFileName().toString(); }
        public String getContentType() { return "image/png"; }
        public boolean isEmpty() { return getSize() == 0; }
        public long getSize() { try { return Files.size(path); } catch(IOException e) { throw new UncheckedIOException(e); } }
        public byte[] getBytes() { throw new UnsupportedOperationException("Usar streaming"); }
        public InputStream getInputStream() throws IOException { return Files.newInputStream(path); }
        public void transferTo(File dest) throws IOException { Files.copy(path, dest.toPath()); }
    }
    public static void main(String[] args) throws Exception {
        Path carpeta = Path.of(args[0]);
        Properties env = new Properties();
        try (Reader r = Files.newBufferedReader(carpeta.resolve("../.env"), StandardCharsets.UTF_8)) { env.load(r); }
        var config = new SupabaseStorageProperties();
        config.setUrl(env.getProperty("SUPABASE_URL", ""));
        config.setKey(env.getProperty("SUPABASE_STORAGE_KEY", ""));
        config.setBucket(env.getProperty("SUPABASE_STORAGE_BUCKET", "autopartes-imagenes"));
        var mapper = new ObjectMapper();
        var storage = new SupabaseStorageService(config, mapper);
        Properties db = new Properties();
        db.setProperty("user", "postgres"); db.setProperty("password", env.getProperty("DB_PASSWORD"));
        db.setProperty("sslmode", "require"); db.setProperty("connectTimeout", "20");
        List<StorageImageResponse> cargadas = new ArrayList<>();
        List<Map<String,Object>> manifiesto = new ArrayList<>();
        boolean committed = false;
        try (Connection c = DriverManager.getConnection("jdbc:postgresql://db.ruvuaponemmepdwnqfce.supabase.co:5432/postgres", db)) {
            c.setAutoCommit(false);
            try {
                try(Statement lock = c.createStatement()) { lock.execute("select pg_advisory_xact_lock(183476521)"); }
                for (String sku : SKUS) {
                    Path archivo = carpeta.resolve("imagenes/" + sku + ".png");
                    if (!Files.isRegularFile(archivo)) throw new IllegalStateException("Falta la imagen de " + sku);
                    UUID id; String anterior;
                    try (PreparedStatement query = c.prepareStatement("select id, imagen_url from producto where sku=? for update")) {
                        query.setString(1, sku);
                        try (ResultSet r = query.executeQuery()) {
                            if (!r.next()) throw new IllegalStateException("No existe el producto " + sku);
                            id = r.getObject("id", UUID.class); anterior = r.getString("imagen_url");
                        }
                    }
                    StorageImageResponse imagen = storage.crear(new Archivo(archivo), "productos");
                    cargadas.add(imagen);
                    try (PreparedStatement update = c.prepareStatement("update producto set imagen_url=?, fecha_actualizacion=now() where id=?")) {
                        update.setString(1, imagen.url()); update.setObject(2, id);
                        if (update.executeUpdate() != 1) throw new IllegalStateException("No se actualizó " + sku);
                    }
                    Map<String,Object> registro = new LinkedHashMap<>();
                    registro.put("sku", sku); registro.put("id", id.toString()); registro.put("archivo", sku + ".png");
                    registro.put("ruta", imagen.ruta()); registro.put("url", imagen.url()); registro.put("urlAnterior", anterior);
                    registro.put("size", imagen.size()); manifiesto.add(registro);
                    System.out.println("Imagen cargada y vinculada: " + sku);
                }
                // Guardar el manifiesto antes del commit: si no se puede escribir, revertir y limpiar.
                Path destino = carpeta.resolve("imagenes/registros-" + System.currentTimeMillis() + ".json");
                mapper.writerWithDefaultPrettyPrinter().writeValue(destino.toFile(), manifiesto);
                c.commit(); committed = true;
                System.out.println("Confirmados 5 registros de productos; no se modificaron precios, categorías ni existencias.");
                System.out.println("Manifiesto: " + destino.toAbsolutePath());
            } catch(Exception e) { c.rollback(); throw e; }
        } finally {
            if (!committed) {
                for (StorageImageResponse imagen : cargadas) {
                    try { storage.eliminar(imagen.ruta()); }
                    catch (Exception cleanup) { System.err.println("Revisar carga incompleta: " + imagen.ruta()); }
                }
            }
        }
    }
}
