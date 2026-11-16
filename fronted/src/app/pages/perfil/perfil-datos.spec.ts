import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, throwError, Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MiPerfilPage } from './mi-perfil/mi-perfil';
import { MisPedidosPage } from './mis-pedidos/mis-pedidos';
import { PerfilService } from '../../_services/perfil.service';
import { PerfilModalService } from '../../_services/perfil-modal.service';
const usuario = { id: 'u1', nombre: 'Ana', apellido: 'Perez', correo: 'ana@example.com', telefono: '', direccion: '', fotoPerfilUrl: '' };
const pedidos = [ { id: 'o1', numeroOrden: 'ORD-001', estado: 'PENDIENTE', fechaCreacion: '2026-11-01', total: 50 }, { id: 'o2', numeroOrden: 'ORD-002', estado: 'ENTREGADO', fechaCreacion: '2026-11-10', total: 90 } ];
describe('Datos de perfil y pedidos', () => {
 let service: any;
 beforeEach(() => {
  service = { obtener: vi.fn(() => of(usuario)), guardar: vi.fn(() => of({ ...usuario, nombre: 'Maria' })), subirFoto: vi.fn(() => of({ ...usuario, fotoPerfilUrl: '/api/storage/imagenes/perfiles/nueva.png' })), pedidos: vi.fn(() => of(pedidos)), items: vi.fn(() => of([{ id: 'i1', nombreProducto: 'Aceite', cantidad: 2, subtotal: 50 }])) };
  TestBed.configureTestingModule({ imports: [MiPerfilPage, MisPedidosPage], providers: [provideHttpClient(),{ provide: PerfilService, useValue: service }, { provide: PerfilModalService, useValue: { cerrar: vi.fn() } }] });
 });
 it('carga datos y guarda unicamente los campos editables', () => {
  const fixture = TestBed.createComponent(MiPerfilPage); const page = fixture.componentInstance; fixture.detectChanges();
  page.form.controls.nombre.setValue(' Maria '); page.guardar();
  expect(service.guardar).toHaveBeenCalledWith({ nombre: 'Maria', apellido: 'Perez', telefono: '', direccion: '', fotoPerfilUrl: '' });
  expect(page.usuario?.nombre).toBe('Maria'); expect(page.form.pristine).toBe(true);
 });
 it('rechaza nombres vacíos y mantiene el formulario al fallar', () => {
  const page = TestBed.createComponent(MiPerfilPage).componentInstance; page.ngOnInit();
  page.form.controls.nombre.setValue(' '); page.guardar(); expect(service.guardar).not.toHaveBeenCalled();
  page.form.controls.nombre.setValue('Maria'); service.guardar.mockReturnValueOnce(throwError(() => new Error('Sin conexión'))); page.guardar();
  expect(page.form.controls.nombre.value).toBe('Maria'); expect(page.guardando).toBe(false); expect(page.form.enabled).toBe(true);
 });
 it('descarta cambios sin guardarlos', () => {
  const page = TestBed.createComponent(MiPerfilPage).componentInstance; page.ngOnInit(); page.form.controls.nombre.setValue('Otra'); page.cancelar();
  expect(page.form.controls.nombre.value).toBe('Ana'); expect(service.guardar).not.toHaveBeenCalled();
 });
 it('muestra los pedidos recientes y filtra por número y estado', () => {
  const page = TestBed.createComponent(MisPedidosPage).componentInstance; page.ngOnInit();
  expect(page.visibles[0].id).toBe('o2'); page.estado = 'PENDIENTE'; expect(page.filtrados.map(p => p.id)).toEqual(['o1']);
  page.busqueda = 'ORD-002'; expect(page.filtrados).toEqual([]); page.estado = ''; expect(page.filtrados.length).toBe(1);
 });
 it('carga el detalle y no mezcla respuestas de otros pedidos', () => {
  const page = TestBed.createComponent(MisPedidosPage).componentInstance; page.ngOnInit();
  const anterior = new Subject<any[]>(); service.items.mockReturnValueOnce(anterior); page.verDetalle(page.pedidos[0]);
  page.verDetalle(page.pedidos[1]); anterior.next([{ id: 'incorrecto' }]);
  expect(page.items[0].id).toBe('i1'); expect(page.seleccionado?.id).toBe('o1'); expect(service.items).toHaveBeenLastCalledWith('o1');
 });
 it('permite reintentar cuando falla el historial', () => {
  const page = TestBed.createComponent(MisPedidosPage).componentInstance;
  service.pedidos.mockReturnValueOnce(throwError(() => new Error('Sin conexión'))); page.cargar(); expect(page.error).not.toBe('');
  page.cargar(); expect(page.error).toBe(''); expect(page.pedidos.length).toBe(2);
 });
});
