package com.autopartes.service;
import com.autopartes.model.*;
import com.autopartes.repository.UsuarioRepository;
import com.autopartes.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
class UsuarioRolServiceTest {
 UsuarioRepository repo; UsuarioService service; Usuario admin; Usuario destino;
 @BeforeEach void datos(){repo=mock(UsuarioRepository.class);service=new UsuarioService(repo,mock(PasswordEncoder.class));admin=new Usuario();admin.setId(UUID.randomUUID());admin.setRol(Rol.ADMIN);admin.setActivo(true);destino=new Usuario();destino.setId(UUID.randomUUID());destino.setRol(Rol.CLIENTE);destino.setCorreo("cliente@example.com");destino.setContrasena("hash-original");when(repo.buscarPorCorreo("admin@example.com")).thenReturn(Optional.of(admin));when(repo.buscarPorId(destino.getId())).thenReturn(Optional.of(destino));when(repo.guardar(any())).thenAnswer(i->i.getArgument(0));}
 @Test void cambiaRolSinAlterarCorreoNiContrasena(){var u=service.cambiarRol(destino.getId(),Rol.EMPLEADO,"admin@example.com");assertEquals(Rol.EMPLEADO,u.getRol());assertEquals("cliente@example.com",u.getCorreo());assertEquals("hash-original",u.getContrasena());verify(repo).guardar(destino);}
 @Test void noPermiteCambiarRolPropio(){assertThrows(BusinessException.class,()->service.cambiarRol(admin.getId(),Rol.CLIENTE,"admin@example.com"));verify(repo,never()).guardar(any());}
 @Test void compruebaQueElActorSeaAdministradorActivo(){admin.setRol(Rol.EMPLEADO);assertThrows(BusinessException.class,()->service.cambiarRol(destino.getId(),Rol.ADMIN,"admin@example.com"));verify(repo,never()).guardar(any());}
 @Test void rolIgualNoProvocaOtraEscritura(){assertSame(destino,service.cambiarRol(destino.getId(),Rol.CLIENTE,"admin@example.com"));verify(repo,never()).guardar(any());}
}
