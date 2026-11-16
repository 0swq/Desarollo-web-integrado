package com.autopartes.service;
import com.autopartes.model.ParametroTienda;
import com.autopartes.dto.ParametrosRequest;
import com.autopartes.repository.ParametroTiendaRepository;
import com.autopartes.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Set;
@Service
public class ParametrosService {
 private final ParametroTiendaRepository repository;
 public ParametrosService(ParametroTiendaRepository repository){this.repository=repository;}
 public ParametroTienda obtener(){return repository.findById(1L).orElseThrow(()->new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Los parámetros todavía no están disponibles"));}
 @Transactional public ParametroTienda actualizar(ParametrosRequest r){
  var p=obtener();
  if(!p.getVersion().equals(r.version()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Otro administrador actualizó los parámetros. Recarga antes de guardar.");
  String maps=r.mapsUrl()==null?"":r.mapsUrl().trim();
  if(!maps.isEmpty()){
   try{var uri=URI.create(maps);String host=uri.getHost();String path=uri.getPath()==null?"":uri.getPath();
    boolean valido=(Set.of("google.com","www.google.com").contains(host) && (path.equals("/maps") || path.startsWith("/maps/"))) || "maps.google.com".equals(host) || ("maps.app.goo.gl".equals(host) && path.length()>1) || ("goo.gl".equals(host) && path.startsWith("/maps/") && path.length()>6);
    if(!"https".equals(uri.getScheme()) || !valido || uri.getUserInfo()!=null || uri.getPort()!=-1)throw new IllegalArgumentException();}
   catch(Exception e){throw new BusinessException("El enlace del mapa debe ser una URL https de Google Maps");}
  }
  p.setNombre(r.nombre().trim());p.setLocal(r.local().trim());p.setTelefono(r.telefono().trim());p.setWhatsapp(r.whatsapp());p.setDireccion(r.direccion().trim());p.setCorreoContacto(r.correoContacto()==null?"":r.correoContacto().trim());p.setHorario(r.horario()==null?"":r.horario().trim());p.setLatitud(r.latitud());p.setLongitud(r.longitud());p.setMapsUrl(maps);
  return repository.saveAndFlush(p);
 }
 @Transactional public void inicializar(){
  if(repository.existsById(1L))return;
  var p=new ParametroTienda();p.setId(1L);p.setNombre("Autopartes Quiroz");p.setLocal("Multiservicios e Inversiones QUIROZ");p.setTelefono("940 061 944");p.setWhatsapp("51940061944");p.setDireccion("Dirección por confirmar");p.setCorreoContacto("");p.setHorario("");p.setLatitud(new BigDecimal("-9.1374281"));p.setLongitud(new BigDecimal("-78.5063972"));p.setMapsUrl("https://www.google.com/maps/place/Multiservicios+e+Inversiones+QUIROZ/data=!4m7!3m6!1s0x91ab852fd96e0d23:0x5ac0eab52c212a80!4b1!8m2!3d-9.1374281!4d-78.5063972!16s%2Fg%2F11vhxdq5m_");repository.saveAndFlush(p);
 }
}
