import { TestBed } from '@angular/core/testing';
import { of, throwError, Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SeguridadPage } from './seguridad/seguridad';
import { PerfilService } from '../../_services/perfil.service';
describe('Seguridad de la cuenta', () => {
 let service: any; let page: SeguridadPage;
 beforeEach(() => {
  service = { obtener: vi.fn(() => of({ correo: 'ana@example.com' })), cambiarPassword: vi.fn(() => of(undefined)), cambiarCorreo: vi.fn(() => of({ correo: 'nuevo@example.com' })) };
  TestBed.configureTestingModule({ imports: [SeguridadPage], providers: [{ provide: PerfilService, useValue: service }] });
  page = TestBed.createComponent(SeguridadPage).componentInstance; page.ngOnInit();
 });
 it('rechaza contraseñas distintas o demasiado cortas', () => {
  page.password.setValue({ contrasenaActual: 'Anterior123', contrasenaNueva: 'Corta', confirmacionContrasena: 'Corta' }); page.guardarPassword();
  page.password.setValue({ contrasenaActual: 'Anterior123', contrasenaNueva: 'Nueva123456', confirmacionContrasena: 'Otra123456' }); page.guardarPassword();
  expect(service.cambiarPassword).not.toHaveBeenCalled();
 });
 it('cambia contraseña y limpia los campos sensibles', () => {
  page.password.setValue({ contrasenaActual: 'Anterior123', contrasenaNueva: 'Nueva123456', confirmacionContrasena: 'Nueva123456' }); page.guardarPassword();
  expect(service.cambiarPassword).toHaveBeenCalledOnce(); expect(page.password.controls.contrasenaActual.value).toBe(''); expect(page.ocupado).toBe('');
 });
 it('muestra errores del servidor y permite corregirlos', () => {
  service.cambiarPassword.mockReturnValueOnce(throwError(() => ({ error: { message: 'La contraseña actual es incorrecta' } })));
  page.password.setValue({ contrasenaActual: 'Anterior123', contrasenaNueva: 'Nueva123456', confirmacionContrasena: 'Nueva123456' }); page.guardarPassword();
  expect(page.errorPassword).toBe('La contraseña actual es incorrecta'); expect(page.password.enabled).toBe(true);
 });
 it('confirma correo y actualiza el mostrado después de guardar', () => {
  page.correo.setValue({ correoNuevo: 'nuevo@example.com', confirmacionCorreo: 'otro@example.com', contrasenaActual: 'Anterior123' }); page.guardarCorreo(); expect(service.cambiarCorreo).not.toHaveBeenCalled();
  page.correo.controls.confirmacionCorreo.setValue('nuevo@example.com'); page.guardarCorreo();
  expect(page.correoActual).toBe('nuevo@example.com'); expect(page.correo.controls.contrasenaActual.value).toBe('');
 });
 it('impide envíos duplicados o cambios simultáneos', () => {
  const pendiente = new Subject<void>(); service.cambiarPassword.mockReturnValueOnce(pendiente);
  page.password.setValue({ contrasenaActual: 'Anterior123', contrasenaNueva: 'Nueva123456', confirmacionContrasena: 'Nueva123456' }); page.guardarPassword(); page.guardarPassword(); page.guardarCorreo();
  expect(service.cambiarPassword).toHaveBeenCalledOnce(); expect(service.cambiarCorreo).not.toHaveBeenCalled(); expect(page.correo.disabled).toBe(true); pendiente.next(); pendiente.complete(); expect(page.correo.enabled).toBe(true);
 });
});
