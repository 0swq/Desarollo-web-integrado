import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PerfilService } from './perfil.service';
import { StorageService } from './storage.service';
import { environment } from '../../environments/environment';
describe('Sincronización del rol de sesión',()=>{
 let actual:any;let storage:any;let service:PerfilService;let http:HttpTestingController;
 beforeEach(()=>{
  actual={token:'token',tokenType:'Bearer',id:'u1',correo:'ana@example.com',nombre:'Ana',apellido:'Perez',rol:'CLIENTE',fotoPerfilUrl:null};
  storage={obtenerUsuario:()=>actual,obtenerToken:()=>actual.token,obtenerUsuarioId:()=>actual.id,guardarSesion:vi.fn(u=>actual=u)};
  TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting(),{provide:StorageService,useValue:storage}]});service=TestBed.inject(PerfilService);http=TestBed.inject(HttpTestingController);
 });
 it('reemplaza el rol antiguo con el rol devuelto por el backend',()=>{
  service.obtener().subscribe();http.expectOne(`${environment.apiUrl}/perfil`).flush({success:true,data:{...actual,rol:'ADMIN'}});
  expect(actual.rol).toBe('ADMIN');expect(storage.guardarSesion).toHaveBeenCalledTimes(1);expect(actual.token).toBe('token');http.verify();
 });
 it('no vuelve a emitir cambios cuando la sesión ya coincide',()=>{
  service.obtener().subscribe();http.expectOne(`${environment.apiUrl}/perfil`).flush({success:true,data:{...actual}});
  expect(storage.guardarSesion).not.toHaveBeenCalled();http.verify();
 });
});
