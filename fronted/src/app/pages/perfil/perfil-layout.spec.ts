import { provideHttpClient } from '@angular/common/http';
import { PerfilService } from '../../_services/perfil.service';
import { of } from 'rxjs';
import { provideLocationMocks } from '@angular/common/testing';
import { TestBed } from '@angular/core/testing';
import { Component } from '@angular/core';
import { Location } from '@angular/common';
import { provideRouter, Router, NavigationEnd } from '@angular/router';
import { firstValueFrom, filter, Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PerfilLayout } from './perfil-layout';
import { PerfilModalService } from '../../_services/perfil-modal.service';
import { StorageService } from '../../_services/storage.service';
@Component({ template: '', standalone: true }) class Fondo {}
describe('Historial del modal de perfil', () => {
  let storage: any; let cambios: Subject<void>; let router: Router; let modal: PerfilModalService;
  beforeEach(async () => {
    cambios = new Subject();
    storage = { obtenerToken: vi.fn(() => 'token'), obtenerNombre: () => 'Ana', cambios$: cambios };
    Object.defineProperty(HTMLDialogElement.prototype, 'showModal', { configurable: true, value: function() { this.setAttribute('open', ''); } });
    Object.defineProperty(HTMLDialogElement.prototype, 'close', { configurable: true, value: function() { this.removeAttribute('open'); } });
    TestBed.configureTestingModule({ imports: [PerfilLayout], providers: [provideHttpClient(),{ provide: PerfilService, useValue: { obtener: () => of({ nombre: 'Ana', apellido: 'Perez', correo: 'ana@example.com' }), pedidos: () => of([]) } }, provideLocationMocks(), provideRouter([{ path: 'catalogo', component: Fondo }, { path: 'iniciar-sesion', component: Fondo }]), { provide: StorageService, useValue: storage }] });
    router = TestBed.inject(Router); router.setUpLocationChangeListener(); modal = TestBed.inject(PerfilModalService);
    await router.navigateByUrl('/catalogo?marca=toyota#productos');
  });
  it('mantiene fondo, filtros y fragmento al abrir y cerrar', async () => {
    const fixture = TestBed.createComponent(PerfilLayout); fixture.detectChanges();
    await modal.abrir(); fixture.detectChanges();
    expect(router.url).toBe('/catalogo?marca=toyota&perfil=mi-perfil#productos');
    expect(fixture.nativeElement.querySelector('dialog').open).toBe(true);
    await modal.cerrar(); fixture.detectChanges();
    expect(router.url).toBe('/catalogo?marca=toyota#productos');
    expect(fixture.nativeElement.querySelector('dialog').open).toBe(false); fixture.destroy();
  });
  it('cambia la URL y el contenido de cada sección', async () => {
    const fixture = TestBed.createComponent(PerfilLayout); fixture.detectChanges();
    await modal.abrir('mis-pedidos'); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-mis-pedidos-page')).toBeTruthy();
    expect(router.parseUrl(router.url).queryParams['perfil']).toBe('mis-pedidos');
    await modal.abrir('seguridad'); fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-seguridad-page')).toBeTruthy();
    await modal.cerrar(); fixture.detectChanges(); fixture.destroy();
  });
  it('Atrás reabre el modal cerrado y Adelante vuelve a cerrarlo', async () => {
    await modal.abrir('mis-pedidos'); await modal.cerrar();
    const location = TestBed.inject(Location);
    let navigation = firstValueFrom(router.events.pipe(filter(e => e instanceof NavigationEnd)));
    location.back(); await navigation;
    expect(modal.abierto()).toBe(true); expect(modal.seccion()).toBe('mis-pedidos');
    navigation = firstValueFrom(router.events.pipe(filter(e => e instanceof NavigationEnd)));
    location.forward(); await navigation; expect(modal.abierto()).toBe(false);
  });
  it('restaura el modal desde una URL directa', async () => {
    await router.navigateByUrl('/catalogo?perfil=seguridad');
    expect(modal.abierto()).toBe(true); expect(modal.seccion()).toBe('seguridad');
  });
  it('no abre una sección desconocida o sin sesión', async () => {
    await router.navigateByUrl('/catalogo?perfil=otro'); expect(modal.abierto()).toBe(false);
    storage.obtenerToken.mockReturnValue(null); await modal.abrir();
    expect(router.url).toBe('/iniciar-sesion'); expect(modal.abierto()).toBe(false);
  });
});
