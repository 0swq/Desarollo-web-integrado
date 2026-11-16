import { provideHttpClient } from '@angular/common/http';
import { PerfilService } from '../../../_services/perfil.service';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject, of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NavbarComponent } from './navbar';
import { CarritoService } from '../../../_services/carrito.service';
import { StorageService } from '../../../_services/storage.service';
import { AuthService } from '../../../_services/auth.service';

describe('Estado del header', () => {
  let cart: any; let storage: any; let page: NavbarComponent;
  let cambiosCarrito: Subject<void>; let cambiosSesion: Subject<void>;
  beforeEach(() => {
    cambiosCarrito = new Subject(); cambiosSesion = new Subject();
    cart = { cambios$: cambiosCarrito, obtenerCarrito: vi.fn(() => of({ totalItems: 3 })) };
    storage = { obtenerRol: vi.fn(() => 'CLIENTE'), cambios$: cambiosSesion, obtenerToken: vi.fn(() => 'token'), obtenerUsuarioId: vi.fn(() => 'u1'), obtenerUsuario: vi.fn(() => ({ fotoPerfilUrl: '/api/storage/avatar.jpg' })) };
    TestBed.configureTestingModule({ imports: [NavbarComponent], providers: [provideHttpClient(),provideRouter([]),
      { provide: PerfilService, useValue: { obtener: vi.fn(() => of(null)) } }, { provide: CarritoService, useValue: cart }, { provide: StorageService, useValue: storage }, { provide: AuthService, useValue: {} },
    ] });
    page = TestBed.createComponent(NavbarComponent).componentInstance;
  });
  it('carga el total del carrito y la foto de la sesión al iniciar', () => {
    expect(cart.obtenerCarrito).toHaveBeenCalledWith('u1'); expect(page.cantidadCarrito()).toBe(3);
    expect(page.fotoPerfil()).toBe('/api/storage/avatar.jpg');
  });
  it('refresca el contador tras modificar el carrito', () => {
    cart.obtenerCarrito.mockReturnValueOnce(of({ totalItems: 5 })); cambiosCarrito.next();
    expect(page.cantidadCarrito()).toBe(5);
  });
  it('limpia contador y foto al cerrar sesión sin consultar el carrito', () => {
    cart.obtenerCarrito.mockClear(); storage.obtenerToken.mockReturnValue(null); cambiosSesion.next();
    expect(page.cantidadCarrito()).toBe(0); expect(page.fotoPerfil()).toBeNull(); expect(cart.obtenerCarrito).not.toHaveBeenCalled();
  });
  it('reintenta la imagen si cambia la foto de perfil', () => {
    page.fotoFallida.set(true); storage.obtenerUsuario.mockReturnValue({ fotoPerfilUrl: '/nuevo.jpg' }); cambiosSesion.next();
    expect(page.fotoPerfil()).toBe('/nuevo.jpg'); expect(page.fotoFallida()).toBe(false);
  });
  it('permite el panel a administradores y empleados pero no a clientes', () => {
    storage.obtenerRol.mockReturnValue('ADMIN'); expect(page.puedeGestionar).toBe(true);
    storage.obtenerRol.mockReturnValue('EMPLEADO'); expect(page.puedeGestionar).toBe(true);
    storage.obtenerRol.mockReturnValue('CLIENTE'); expect(page.puedeGestionar).toBe(false);
    storage.obtenerRol.mockReturnValue('ADMIN'); storage.obtenerToken.mockReturnValue(null); expect(page.puedeGestionar).toBe(false);
  });
  it('consulta el perfil al cargar una sesión y no repite la consulta al cambiar solo el carrito', () => {
    const perfil = TestBed.inject(PerfilService); expect(perfil.obtener).toHaveBeenCalledTimes(1);
    cambiosCarrito.next(); cambiosSesion.next(); expect(perfil.obtener).toHaveBeenCalledTimes(1);
  });
});
