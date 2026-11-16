import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../environments/environment';
import { StorageService } from '../_services/storage.service';
import { authGuard } from './auth.guard';
import { roleGuard } from './role.guard';
import { adminGuard } from './admin-guard';
import { ROLES_GESTION, ROLES_USUARIOS } from '../models/rol';

describe('Protección de rutas', () => {
  const originalAuthEnabled = environment.authEnabled;
  let token: string | null;
  let role: string | null;
  const route = { data: { roles: ['ADMIN'] } } as unknown as ActivatedRouteSnapshot;
  const state = {} as RouterStateSnapshot;

  beforeEach(() => {
    token = null;
    role = null;
    environment.authEnabled = true;
    TestBed.configureTestingModule({ providers: [provideRouter([]), {
      provide: StorageService,
      useValue: { obtenerToken: () => token, obtenerRol: () => role },
    }] });
  });
  afterEach(() => { environment.authEnabled = originalAuthEnabled; });

  it('permite probar sin sesión cuando la protección está desactivada', () => {
    environment.authEnabled = false;
    expect(TestBed.runInInjectionContext(() => authGuard(route, state))).toBe(true);
    expect(TestBed.runInInjectionContext(() => roleGuard(route, state))).toBe(true);
  });

  it('redirige al login si no existe sesión', () => {
    const result = TestBed.runInInjectionContext(() => authGuard(route, state));
    expect(TestBed.inject(Router).serializeUrl(result as never)).toBe('/iniciar-sesion');
  });

  it('permite acceso autenticado', () => {
    token = 'test-token';
    expect(TestBed.runInInjectionContext(() => authGuard(route, state))).toBe(true);
  });

  it('impide el acceso con un rol diferente al requerido', () => {
    token = 'test-token';
    role = 'CLIENTE';
    const result = TestBed.runInInjectionContext(() => roleGuard(route, state));
    expect(TestBed.inject(Router).serializeUrl(result as never)).toBe('/catalogo');
  });

  it('permite el rol requerido', () => {
    token = 'test-token';
    role = 'ADMIN';
    expect(TestBed.runInInjectionContext(() => roleGuard(route, state))).toBe(true);
  });

  it.each([
    ['ADMIN', true], ['EMPLEADO', true], ['CLIENTE', false],
  ])('gestión: %s respeta SecurityConfig', (userRole, allowed) => {
    token = 'test-token';
    role = userRole;
    const managementRoute = { data: { roles: ROLES_GESTION } } as unknown as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() => roleGuard(managementRoute, state));
    expect(result === true).toBe(allowed);
  });

  it.each([
    ['ADMIN', true], ['EMPLEADO', false], ['CLIENTE', false],
  ])('usuarios: %s respeta el acceso exclusivo ADMIN', (userRole, allowed) => {
    token = 'test-token';
    role = userRole;
    const usersRoute = { data: { roles: ROLES_USUARIOS } } as unknown as ActivatedRouteSnapshot;
    expect(TestBed.runInInjectionContext(() => roleGuard(usersRoute, state)))
      .toEqual(TestBed.runInInjectionContext(() => adminGuard(usersRoute, state)));
    expect(TestBed.runInInjectionContext(() => roleGuard(usersRoute, state)) === true).toBe(allowed);
  });

  it.each(['ADMIN', 'EMPLEADO', 'CLIENTE'])('carrito: permite cualquier sesión %s', userRole => {
    token = 'test-token';
    role = userRole;
    expect(TestBed.runInInjectionContext(() => authGuard(route, state))).toBe(true);
  });
});
