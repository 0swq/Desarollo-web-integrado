import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class SeedProductos {
  static UUID id(Connection c,String sql,String valor) throws SQLException {
    try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,valor);try(ResultSet r=s.executeQuery()){return r.next()?r.getObject(1,UUID.class):null;}}
  }
  static void ejecutar(Connection c,String sql,Object... valores) throws SQLException {
    try(PreparedStatement s=c.prepareStatement(sql)){for(int i=0;i<valores.length;i++)s.setObject(i+1,valores[i]);s.executeUpdate();}
  }
  public static void main(String[] args) throws Exception {
    Path carpeta=Path.of(args[0]);
    String clave=Files.readAllLines(carpeta.resolve("../.env"),StandardCharsets.UTF_8).stream().filter(l->l.startsWith("DB_PASSWORD=")).map(l->l.substring(12).trim()).findFirst().orElseThrow();
    Properties p=new Properties();p.setProperty("user","postgres");p.setProperty("password",clave);p.setProperty("sslmode","require");p.setProperty("connectTimeout","20");
    String[][] productos={
      {"DEMO-ACE-5W30-1L","Aceite de motor sintético 5W-30 · 1 L","Lubricantes y fluidos","45.00","39.90","24","Producto de demostración. Envase de 1 litro, viscosidad SAE 5W-30. Verificar las especificaciones y aprobaciones exigidas por el fabricante del vehículo antes de usarlo."},
      {"DEMO-REF-5050-1L","Refrigerante premezclado 50/50 · 1 L","Refrigeración","28.00","","18","Producto de demostración. Refrigerante premezclado en envase de 1 litro. La tecnología y compatibilidad química deben verificarse según el vehículo; no mezclar formulaciones sin confirmación."},
      {"DEMO-LUZ-H4-12V","Lámpara halógena H4 · 12 V · 60/55 W","Iluminación","22.00","18.90","30","Producto de demostración. Lámpara H4 de doble filamento, 12 V y 60/55 W. Confirmar el tipo de portalámparas y la potencia admitida por el faro."},
      {"DEMO-PLU-20IN","Plumilla limpiaparabrisas · 20 pulgadas","Limpiaparabrisas","35.00","","12","Producto de demostración. Plumilla de 20 pulgadas (508 mm). Confirmar longitud, lado de montaje y tipo de anclaje del brazo antes de instalar."},
      {"DEMO-FIL-ACE-001","Filtro de aceite de motor · referencia de prueba","Filtros","32.00","","0","Producto de demostración sin equivalencia OEM asignada. No tiene compatibilidad de vehículo validada; confirmar rosca, junta, dimensiones y válvulas con la referencia original."}
    };
    try(Connection c=DriverManager.getConnection("jdbc:postgresql://db.ruvuaponemmepdwnqfce.supabase.co:5432/postgres",p)){
      c.setAutoCommit(false);
      try{
        try(Statement s=c.createStatement()){s.execute("select pg_advisory_xact_lock(183476521)");}
        int nuevos=0;
        for(String[] v:productos){
          if(id(c,"select id from producto where sku=?",v[0])!=null)continue;
          UUID cat=id(c,"select id from categoria where nombre=?",v[2]);
          if(cat==null)throw new IllegalStateException("Falta categoría: "+v[2]);
          UUID producto=UUID.randomUUID();
          ejecutar(c,"insert into producto(id,sku,nombre,descripcion,precio,precio_promocional,moneda,activo,fecha_creacion,fecha_actualizacion) values(?,?,?,?,?,?,'PEN',true,now(),now())",producto,v[0],v[1],v[6],new BigDecimal(v[3]),v[4].isEmpty()?null:new BigDecimal(v[4]));
          ejecutar(c,"insert into producto_categoria(producto_id,categoria_id) values(?,?)",producto,cat);
          ejecutar(c,"insert into stock(id,producto_id,cantidad,stock_minimo,ubicacion_almacen,fecha_actualizacion) values(?,?,?,?,?,now())",UUID.randomUUID(),producto,Integer.valueOf(v[5]),5,"DEMO-A01");
          nuevos++;
        }
        c.commit();System.out.println("Productos creados con categoría y stock: "+nuevos);
      }catch(Exception e){c.rollback();throw e;}
    }
  }
}
