import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class SeedCatalogo {
  static UUID existente(Connection c, String sql, Object... args) throws SQLException {
    try (PreparedStatement s = c.prepareStatement(sql)) {
      for(int i=0;i<args.length;i++) s.setObject(i+1,args[i]);
      try(ResultSet r=s.executeQuery()) { return r.next() ? r.getObject(1,UUID.class) : null; }
    }
  }
  static void insertar(Connection c, String sql, Object... args) throws SQLException {
    try (PreparedStatement s=c.prepareStatement(sql)) {
      for(int i=0;i<args.length;i++) s.setObject(i+1,args[i]);
      s.executeUpdate();
    }
  }
  static long contar(Connection c,String tabla) throws SQLException {
    if(!Set.of("producto","categoria","marca_vehiculo","modelo_vehiculo").contains(tabla)) throw new IllegalArgumentException();
    try(Statement s=c.createStatement(); ResultSet r=s.executeQuery("select count(*) from "+tabla)){r.next();return r.getLong(1);}
  }
  public static void main(String[] args) throws Exception {
    Path carpeta=Path.of(args[0]);
    String password=Files.readAllLines(carpeta.resolve("../.env"),StandardCharsets.UTF_8).stream()
      .filter(l->l.startsWith("DB_PASSWORD=")).map(l->l.substring("DB_PASSWORD=".length()).trim()).findFirst().orElseThrow();
    Properties props=new Properties();props.setProperty("user","postgres");props.setProperty("password",password);props.setProperty("sslmode","require");props.setProperty("connectTimeout","20");
    try(Connection c=DriverManager.getConnection("jdbc:postgresql://db.ruvuaponemmepdwnqfce.supabase.co:5432/postgres",props)) {
      c.setAutoCommit(false);
      try {
        try(Statement lock=c.createStatement()){lock.execute("select pg_advisory_xact_lock(183476521)");}
        long antes=contar(c,"producto");int cats=0,marcas=0,modelos=0;
        for(String linea:Files.readAllLines(carpeta.resolve("categorias.csv"),StandardCharsets.UTF_8).subList(1,Files.readAllLines(carpeta.resolve("categorias.csv"),StandardCharsets.UTF_8).size())) {
          if(linea.isBlank())continue;
          String[] v=linea.split("\\|",-1);
          if(existente(c,"select id from categoria where lower(nombre)=lower(?) limit 1",v[0])==null){
            insertar(c,"insert into categoria(id,nombre,descripcion,activo,fecha_creacion,fecha_actualizacion) values(?,?,?,true,now(),now())",UUID.randomUUID(),v[0],v[1]);cats++;
          }
        }
        List<String> vehiculos=Files.readAllLines(carpeta.resolve("vehiculos.csv"),StandardCharsets.UTF_8);
        for(String linea:vehiculos.subList(1,vehiculos.size())) {
          if(linea.isBlank())continue;
          String[] v=linea.split("\\|",-1);
          UUID marca=existente(c,"select id from marca_vehiculo where lower(nombre)=lower(?) limit 1",v[0]);
          if(marca==null){marca=UUID.randomUUID();insertar(c,"insert into marca_vehiculo(id,nombre,pais_origen,activo) values(?,?,?,true)",marca,v[0],v[1]);marcas++;}
          if(existente(c,"select id from modelo_vehiculo where marca_id=? and lower(nombre)=lower(?) limit 1",marca,v[2])==null){
            insertar(c,"insert into modelo_vehiculo(id,nombre,marca_id,tipo_vehiculo,activo) values(?,?,?,?,true)",UUID.randomUUID(),v[2],marca,v[3]);modelos++;
          }
        }
        long despues=contar(c,"producto");
        if(antes!=despues)throw new IllegalStateException("El conteo de productos cambió durante la carga; se revierte la transacción.");
        c.commit();
        System.out.printf("Creados: %d categorías, %d marcas, %d modelos.%n",cats,marcas,modelos);
        System.out.printf("Productos antes: %d; después: %d.%n",antes,despues);
      } catch(Exception e) { c.rollback();throw e; }
    }
  }
}
