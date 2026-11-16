import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.Reader;
import java.sql.*;
import java.util.*;

public class SeedCompatibilidades {
    public static void main(String[] args)throws Exception{
        Path root=Path.of(args[0]);Properties env=new Properties();
        try(Reader r=Files.newBufferedReader(root.resolve("../.env"),StandardCharsets.UTF_8)){env.load(r);}
        Properties db=new Properties();db.setProperty("user","postgres");db.setProperty("password",env.getProperty("DB_PASSWORD"));db.setProperty("sslmode","require");db.setProperty("connectTimeout","20");
        String nota="DEMO: asociación ficticia para probar filtros y ficha del catálogo. Años y motor son datos de prueba; NO acredita compatibilidad real. Verificar referencia OEM y ficha técnica antes de usar.";
        List<String> lines=Files.readAllLines(root.resolve("compatibilidades-demo.csv"),StandardCharsets.UTF_8);
        try(Connection c=DriverManager.getConnection("jdbc:postgresql://db.ruvuaponemmepdwnqfce.supabase.co:5432/postgres",db)){
            c.setAutoCommit(false);
            try{
                try(Statement lock=c.createStatement()){lock.execute("select pg_advisory_xact_lock(183476521)");}
                int created=0,existing=0;
                for(String line:lines.subList(1,lines.size())){
                    if(line.isBlank())continue;String[] v=line.split("\\|",-1);
                    if(v.length!=6||!v[0].startsWith("DEMO-"))throw new IllegalStateException("Fila de ejemplo inválida");
                    int desde=Integer.parseInt(v[3]),hasta=Integer.parseInt(v[4]);if(desde<1900||hasta>2100||desde>hasta)throw new IllegalStateException("Rango de años inválido");
                    UUID producto,modelo;
                    try(PreparedStatement s=c.prepareStatement("select id from producto where sku=?")){s.setString(1,v[0]);try(ResultSet r=s.executeQuery()){if(!r.next())throw new IllegalStateException("Falta producto: "+v[0]);producto=r.getObject(1,UUID.class);}}
                    try(PreparedStatement s=c.prepareStatement("select m.id from modelo_vehiculo m join marca_vehiculo b on b.id=m.marca_id where b.nombre=? and m.nombre=?")){s.setString(1,v[1]);s.setString(2,v[2]);try(ResultSet r=s.executeQuery()){if(!r.next())throw new IllegalStateException("Falta modelo: "+v[1]+" "+v[2]);modelo=r.getObject(1,UUID.class);}}
                    try(PreparedStatement s=c.prepareStatement("select id from compatibilidad_vehiculo where producto_id=? and modelo_vehiculo_id=? and anio_inicio=? and anio_fin=? and motor=?")){
                        s.setObject(1,producto);s.setObject(2,modelo);s.setInt(3,desde);s.setInt(4,hasta);s.setString(5,v[5]);
                        try(ResultSet r=s.executeQuery()){if(r.next()){existing++;continue;}}
                    }
                    try(PreparedStatement s=c.prepareStatement("insert into compatibilidad_vehiculo(id,producto_id,modelo_vehiculo_id,anio_inicio,anio_fin,motor,notas) values(?,?,?,?,?,?,?)")){
                        s.setObject(1,UUID.randomUUID());s.setObject(2,producto);s.setObject(3,modelo);s.setInt(4,desde);s.setInt(5,hasta);s.setString(6,v[5]);s.setString(7,nota);s.executeUpdate();created++;
                    }
                }
                c.commit();System.out.println("Compatibilidades DEMO creadas: "+created+"; ya existentes: "+existing);
            }catch(Exception e){c.rollback();throw e;}
        }
    }
}
