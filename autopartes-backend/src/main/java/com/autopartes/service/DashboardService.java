package com.autopartes.service;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
@Service
public class DashboardService {
 private final NamedParameterJdbcTemplate jdbc;
 public DashboardService(NamedParameterJdbcTemplate jdbc){this.jdbc=jdbc;}
 @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
 public Map<String,Object> obtener(LocalDate desde,LocalDate hasta){
 if(desde.isAfter(hasta)||java.time.temporal.ChronoUnit.DAYS.between(desde,hasta)>731)throw new com.autopartes.exception.BusinessException("Selecciona un rango válido de hasta dos años");
 var params=Map.of("desde",desde.atStartOfDay(),"fin",hasta.plusDays(1).atStartOfDay());
 String base="""
 WITH ventas AS (SELECT o.* FROM orden o WHERE o.fecha_creacion>=:desde AND o.fecha_creacion<:fin
 AND o.estado<>'CANCELADO' AND coalesce(o.moneda,'PEN')='PEN'
 AND EXISTS(SELECT 1 FROM pago p WHERE p.orden_id=o.id AND p.estado='APROBADO' AND coalesce(p.moneda,'PEN')='PEN'))
 """;
 Map<String,Object> result=new LinkedHashMap<>();result.put("desde",desde);result.put("hasta",hasta);result.put("moneda","PEN");
 result.put("resumen",jdbc.queryForMap(base+"SELECT coalesce(sum(total),0) AS ventas,count(*) AS pedidos,coalesce(avg(total),0) AS ticket,coalesce((SELECT sum(i.cantidad) FROM item_orden i JOIN ventas v ON v.id=i.orden_id),0) AS unidades FROM ventas",params));
 result.put("mensual",jdbc.queryForList(base+"SELECT to_char(m,'YYYY-MM') AS mes,coalesce(sum(v.total),0) AS monto FROM generate_series(date_trunc('month',cast(:desde AS timestamp)),date_trunc('month',cast(:fin AS timestamp)-interval '1 day'),interval '1 month') m LEFT JOIN ventas v ON date_trunc('month',v.fecha_creacion)=m GROUP BY m ORDER BY m",params));
 result.put("categorias",jdbc.queryForList(base+"SELECT coalesce(c.nombre,'Sin categoría') AS nombre,coalesce(sum(i.subtotal),0) AS monto FROM item_orden i JOIN ventas v ON v.id=i.orden_id LEFT JOIN LATERAL (SELECT pc.categoria_id FROM producto_categoria pc WHERE pc.producto_id=i.producto_id ORDER BY pc.categoria_id LIMIT 1) pc ON true LEFT JOIN categoria c ON c.id=pc.categoria_id GROUP BY c.id,c.nombre ORDER BY monto DESC",params));
 result.put("productos",jdbc.queryForList(base+"SELECT i.sku,max(i.nombre_producto) AS nombre,sum(i.cantidad) AS cantidad,sum(i.subtotal) AS total FROM item_orden i JOIN ventas v ON v.id=i.orden_id GROUP BY i.sku ORDER BY cantidad DESC,i.sku LIMIT 10",params));
 result.put("semanal",jdbc.queryForList(base+"SELECT d AS dia,count(v.id) AS cantidad FROM generate_series(1,7) d LEFT JOIN ventas v ON extract(isodow FROM v.fecha_creacion)=d GROUP BY d ORDER BY d",params));
 return result;
 }
}