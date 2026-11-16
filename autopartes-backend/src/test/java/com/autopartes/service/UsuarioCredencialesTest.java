package com.autopartes.service;
import com.autopartes.dto.auth.*;
import com.autopartes.model.*;
import com.autopartes.repository.UsuarioRepository;
import com.autopartes.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class UsuarioCredencialesTest {
 UsuarioRepository repo; PasswordEncoder encoder; UsuarioService service; Usuario usuario;
 @BeforeEach void preparar() {
  repo=mock(UsuarioRepository.class); encoder=mock(PasswordEncoder.class); service=new UsuarioService(repo,encoder);
  usuario=new Usuario(); usuario.setId(UUID.randomUUID()); usuario.setCorreo("ana@example.com"); usuario.setContrasena("hash-anterior"); usuario.setRol(Rol.CLIENTE);
  when(repo.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario)); when(encoder.matches("Anterior123", "hash-anterior")).thenReturn(true);
 }
 @Test void passwordIncorrectaNoGuarda() {
  assertThrows(BusinessException.class, () -> service.cambiarPasswordPropio(usuario.getId(),new CambioPasswordRequest("incorrecta","Nueva123456","Nueva123456")));
  verify(repo,never()).guardar(any());
 }
 @Test void confirmacionDistintaNoGuarda() {
  assertThrows(BusinessException.class, () -> service.cambiarPasswordPropio(usuario.getId(),new CambioPasswordRequest("Anterior123","Nueva123456","diferente")));
  verify(repo,never()).guardar(any());
 }
 @Test void nuevaDistintaSeGuardaCifrada() {
  when(encoder.encode("Nueva123456")).thenReturn("hash-nuevo");
  service.cambiarPasswordPropio(usuario.getId(),new CambioPasswordRequest("Anterior123","Nueva123456","Nueva123456"));
  assertEquals("hash-nuevo",usuario.getContrasena()); verify(repo).guardar(usuario);
 }
 @Test void noPermiteReutilizarPassword() {
  assertThrows(BusinessException.class, () -> service.cambiarPasswordPropio(usuario.getId(),new CambioPasswordRequest("Anterior123","Anterior123","Anterior123")));
  verify(repo,never()).guardar(any());
 }
 @Test void rechazaCorreoOcupado() {
  when(repo.existePorCorreo("otro@example.com")).thenReturn(true);
  assertThrows(BusinessException.class, () -> service.cambiarCorreoPropio(usuario.getId(),new CambioCorreoRequest("otro@example.com","otro@example.com","Anterior123")));
  assertEquals("ana@example.com",usuario.getCorreo()); verify(repo,never()).saveAndFlush(any());
 }
 @Test void correoRequierePasswordActual() {
  assertThrows(BusinessException.class, () -> service.cambiarCorreoPropio(usuario.getId(),new CambioCorreoRequest("nuevo@example.com","nuevo@example.com","incorrecta")));
  verify(repo,never()).saveAndFlush(any());
 }
 @Test void correoNormalizadoConservaRolYPassword() {
  when(repo.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
  var actualizado=service.cambiarCorreoPropio(usuario.getId(),new CambioCorreoRequest("NUEVO@example.com","nuevo@example.com","Anterior123"));
  assertEquals("nuevo@example.com",actualizado.getCorreo()); assertEquals(Rol.CLIENTE,actualizado.getRol()); assertEquals("hash-anterior",actualizado.getContrasena());
 }
 @Test void correoConfirmadoIncorrectamenteNoGuarda() {
  assertThrows(BusinessException.class, () -> service.cambiarCorreoPropio(usuario.getId(),new CambioCorreoRequest("nuevo@example.com","distinto@example.com","Anterior123")));
  verify(repo,never()).saveAndFlush(any());
 }
}
