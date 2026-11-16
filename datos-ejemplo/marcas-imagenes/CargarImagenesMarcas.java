import com.autopartes.storage.*;
import com.fasterxml.jackson.databind.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class CargarImagenesMarcas {
    static class Archivo implements MultipartFile {
        final Path file;Archivo(Path file){this.file=file;}
        public String getName(){return "archivo";}public String getOriginalFilename(){return file.getFileName().toString();}public String getContentType(){return "image/png";}
        public boolean isEmpty(){return getSize()==0;}public long getSize(){try{return Files.size(file);}catch(IOException e){throw new UncheckedIOException(e);}}
        public byte[] getBytes(){throw new UnsupportedOperationException("Usar streaming");}public InputStream getInputStream()throws IOException{return Files.newInputStream(file);}
        public void transferTo(File dest)throws IOException{Files.copy(file,dest.toPath());}
    }
    public static void main(String[] args)throws Exception{
        Path root=Path.of(args[0]);Properties env=new Properties();try(Reader r=Files.newBufferedReader(root.resolve("../../.env"),StandardCharsets.UTF_8)){env.load(r);}
        ObjectMapper mapper=new ObjectMapper();JsonNode assets=mapper.readTree(root.resolve("seleccion.json").toFile());
        SupabaseStorageProperties config=new SupabaseStorageProperties();config.setUrl(env.getProperty("SUPABASE_URL"));config.setKey(env.getProperty("SUPABASE_STORAGE_KEY"));config.setBucket(env.getProperty("SUPABASE_STORAGE_BUCKET","autopartes-imagenes"));config.setCreateBucket(false);
        SupabaseStorageService storage=new SupabaseStorageService(config,mapper);Properties db=new Properties();db.setProperty("user","postgres");db.setProperty("password",env.getProperty("DB_PASSWORD"));db.setProperty("sslmode","require");db.setProperty("connectTimeout","20");
        List<StorageImageResponse> nuevas=new ArrayList<>();boolean committed=false;
        try(Connection c=DriverManager.getConnection("jdbc:postgresql://db.ruvuaponemmepdwnqfce.supabase.co:5432/postgres",db)){
            try(Statement s=c.createStatement()){s.execute("alter table marca_vehiculo add column if not exists imagen_url varchar(2048)");}
            c.setAutoCommit(false);
            try{
                List<Map<String,Object>> manifest=new ArrayList<>();
                for(JsonNode asset:assets){
                    UUID id;String previous;String nombre=asset.path("marca").asText();
                    try(PreparedStatement s=c.prepareStatement("select id,imagen_url from marca_vehiculo where nombre=? for update")){s.setString(1,nombre);try(ResultSet r=s.executeQuery()){if(!r.next())throw new IllegalStateException("Falta marca: "+nombre);id=r.getObject(1,UUID.class);previous=r.getString(2);}}
                    StorageImageResponse image=storage.crear(new Archivo(root.resolve(asset.path("archivo").asText())),"marcas");nuevas.add(image);
                    try(PreparedStatement s=c.prepareStatement("update marca_vehiculo set imagen_url=? where id=?")){s.setString(1,image.url());s.setObject(2,id);if(s.executeUpdate()!=1)throw new IllegalStateException("No se actualizó "+nombre);}
                    Map<String,Object> row=new LinkedHashMap<>();row.put("id",id.toString());row.put("marca",nombre);row.put("archivo",asset.path("archivo").asText());row.put("ruta",image.ruta());row.put("url",image.url());row.put("urlAnterior",previous);manifest.add(row);System.out.println("Logo cargado: "+nombre);
                }
                Path destino=root.resolve("registros-"+System.currentTimeMillis()+".json");mapper.writerWithDefaultPrettyPrinter().writeValue(destino.toFile(),manifest);c.commit();committed=true;System.out.println("Marcas con imagen: "+manifest.size());
            }catch(Exception e){c.rollback();throw e;}
        }finally{if(!committed)for(StorageImageResponse image:nuevas){try{storage.eliminar(image.ruta());}catch(Exception e){System.err.println("Revisar carga: "+image.ruta());}}}
    }
}
