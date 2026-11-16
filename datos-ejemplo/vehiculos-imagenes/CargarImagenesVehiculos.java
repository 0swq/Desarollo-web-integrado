import com.autopartes.storage.*;
import com.fasterxml.jackson.databind.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CargarImagenesVehiculos {
    static class Archivo implements MultipartFile {
        private final Path path; private final String type;
        Archivo(Path path,String type){this.path=path;this.type=type;}
        public String getName(){return "archivo";} public String getOriginalFilename(){return path.getFileName().toString();}
        public String getContentType(){return type;} public boolean isEmpty(){return getSize()==0;}
        public long getSize(){try{return Files.size(path);}catch(IOException e){throw new UncheckedIOException(e);}}
        public byte[] getBytes(){throw new UnsupportedOperationException("Usar streaming");}
        public InputStream getInputStream()throws IOException{return Files.newInputStream(path);}
        public void transferTo(File dest)throws IOException{Files.copy(path,dest.toPath());}
    }
    record Modelo(UUID id,String marca,String nombre,String anterior,JsonNode asset){}
    public static void main(String[] args)throws Exception{
        Path root=Path.of(args[0]);Properties env=new Properties();
        try(Reader r=Files.newBufferedReader(root.resolve("../../.env"),StandardCharsets.UTF_8)){env.load(r);}
        ObjectMapper mapper=new ObjectMapper();JsonNode assets=mapper.readTree(root.resolve("seleccion.json").toFile());
        SupabaseStorageProperties config=new SupabaseStorageProperties();
        config.setUrl(env.getProperty("SUPABASE_URL",""));config.setKey(env.getProperty("SUPABASE_STORAGE_KEY",""));
        config.setBucket(env.getProperty("SUPABASE_STORAGE_BUCKET","autopartes-imagenes"));config.setCreateBucket(false);
        SupabaseStorageService storage=new SupabaseStorageService(config,mapper);
        Properties db=new Properties();db.setProperty("user","postgres");db.setProperty("password",env.getProperty("DB_PASSWORD"));db.setProperty("sslmode","require");db.setProperty("connectTimeout","20");
        Map<UUID,StorageImageResponse> nuevas=new ConcurrentHashMap<>();boolean committed=false;
        ExecutorService workers=Executors.newFixedThreadPool(4);
        try(Connection c=DriverManager.getConnection("jdbc:postgresql://db.ruvuaponemmepdwnqfce.supabase.co:5432/postgres",db)){
            List<Modelo> modelos=new ArrayList<>();
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("select m.id,m.nombre,m.imagen_url,b.nombre marca from modelo_vehiculo m join marca_vehiculo b on b.id=m.marca_id order by b.nombre,m.nombre")){
                while(r.next()){
                    JsonNode asset=null;String marca=r.getString("marca"),nombre=r.getString("nombre");
                    for(JsonNode a:assets)if(a.path("marca").asText().equals(marca)&&a.path("modelo").asText().equals(nombre)){asset=a;break;}
                    if(asset==null||asset.has("error")||!asset.has("archivo"))throw new IllegalStateException("Falta imagen: "+marca+" "+nombre);
                    Path file=root.resolve(asset.path("archivo").asText()).normalize();
                    if(!file.toAbsolutePath().startsWith(root.toAbsolutePath().normalize())||!Files.isRegularFile(file))throw new IllegalStateException("Archivo no disponible: "+nombre);
                    modelos.add(new Modelo(r.getObject("id",UUID.class),marca,nombre,r.getString("imagen_url"),asset));
                }
            }
            if(modelos.size()!=assets.size())throw new IllegalStateException("La selección de imágenes no coincide con el número actual de modelos.");
            AtomicInteger count=new AtomicInteger();List<Callable<Void>> tareas=new ArrayList<>();
            for(Modelo modelo:modelos)tareas.add(()->{
                JsonNode asset=modelo.asset();StorageImageResponse image=storage.crear(new Archivo(root.resolve(asset.path("archivo").asText()),asset.path("contentType").asText()),"modelos");
                nuevas.put(modelo.id(),image);System.out.println("Subida "+count.incrementAndGet()+"/"+modelos.size()+": "+modelo.marca()+" "+modelo.nombre());return null;
            });
            Exception failure=null;for(Future<Void> future:workers.invokeAll(tareas)){try{future.get();}catch(Exception e){if(failure==null)failure=e;}}
            if(failure!=null)throw failure;
            c.setAutoCommit(false);
            try{
                List<Map<String,Object>> manifiesto=new ArrayList<>();
                for(Modelo modelo:modelos){
                    StorageImageResponse image=nuevas.get(modelo.id());
                    try(PreparedStatement s=c.prepareStatement("update modelo_vehiculo set imagen_url=? where id=? and imagen_url is not distinct from ?")){
                        s.setString(1,image.url());s.setObject(2,modelo.id());s.setString(3,modelo.anterior());
                        if(s.executeUpdate()!=1)throw new IllegalStateException("El registro cambió durante la carga: "+modelo.nombre());
                    }
                    Map<String,Object> registro=new LinkedHashMap<>();registro.put("id",modelo.id().toString());registro.put("marca",modelo.marca());registro.put("modelo",modelo.nombre());registro.put("url",image.url());registro.put("ruta",image.ruta());registro.put("urlAnterior",modelo.anterior());registro.put("archivo",modelo.asset().path("archivo").asText());registro.put("fuente",modelo.asset().path("fuente").asText());manifiesto.add(registro);
                }
                Path manifest=root.resolve("registros-"+System.currentTimeMillis()+".json");mapper.writerWithDefaultPrettyPrinter().writeValue(manifest.toFile(),manifiesto);
                c.commit();committed=true;System.out.println("Confirmados "+modelos.size()+" modelos con imagen de Storage.");System.out.println("Manifiesto: "+manifest.toAbsolutePath());
            }catch(Exception e){c.rollback();throw e;}
        }finally{
            workers.shutdownNow();
            if(!committed)for(StorageImageResponse image:nuevas.values()){try{storage.eliminar(image.ruta());}catch(Exception e){System.err.println("Revisar carga incompleta: "+image.ruta());}}
        }
    }
}
