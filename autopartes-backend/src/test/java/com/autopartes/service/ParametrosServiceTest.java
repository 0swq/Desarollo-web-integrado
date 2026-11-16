package com.autopartes.service;
import com.autopartes.dto.ParametrosRequest;
import com.autopartes.model.ParametroTienda;
import com.autopartes.repository.ParametroTiendaRepository;
import com.autopartes.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
class ParametrosServiceTest {
 @Test void reiniciarNoSobrescribeDatos(){var repo=mock(ParametroTiendaRepository.class);when(repo.existsById(1L)).thenReturn(true);new ParametrosService(repo).inicializar();verify(repo,never()).saveAndFlush(any());}
 private ParametrosRequest datos(String url,Long version){return new ParametrosRequest("Tienda","Local","940061944","51940061944","Calle 123","","",BigDecimal.ZERO,BigDecimal.ZERO,url,version);}
 @Test void rechazaMapaExterno(){var repo=mock(ParametroTiendaRepository.class);var p=new ParametroTienda();p.setVersion(0L);when(repo.findById(1L)).thenReturn(Optional.of(p));assertThrows(BusinessException.class,()->new ParametrosService(repo).actualizar(datos("https://google.com.ejemplo.com/mapa",0L)));verify(repo,never()).saveAndFlush(any());}
 @Test void noSobrescribeUnaEdicionMasReciente(){var repo=mock(ParametroTiendaRepository.class);var p=new ParametroTienda();p.setVersion(2L);when(repo.findById(1L)).thenReturn(Optional.of(p));assertThrows(ResponseStatusException.class,()->new ParametrosService(repo).actualizar(datos("",1L)));verify(repo,never()).saveAndFlush(any());}
 @Test void creaElRegistroInicialSoloSiNoExiste(){var repo=mock(ParametroTiendaRepository.class);new ParametrosService(repo).inicializar();verify(repo).saveAndFlush(argThat(p->p.getId()==1L && "51940061944".equals(p.getWhatsapp()) && p.getLatitud().compareTo(new BigDecimal("-9.1374281"))==0));}
}
