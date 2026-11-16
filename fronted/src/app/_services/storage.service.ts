import { Subject } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { AuthResponse } from '../models/auth.interface';
import { esRol, Rol } from '../models/rol';

@Injectable({ providedIn: 'root' })
export class StorageService {
  private cambios = new Subject<void>();
  readonly cambios$ = this.cambios.asObservable();
  private platformId = inject(PLATFORM_ID);
  private get storage(): Storage | null {
    try {
      return isPlatformBrowser(this.platformId) ? window.localStorage : null;
    } catch {
      return null;
    }
  }
  guardarSesion(data: AuthResponse['data']): void {
    this.storage?.setItem('auth-user', JSON.stringify(data));
    this.storage?.setItem('token', data.token);
    this.storage?.setItem('rol', data.rol);
    this.storage?.setItem('usuarioId', data.id);
    this.cambios.next();
  }
  obtenerUsuario(): AuthResponse['data'] | null {
    const raw = this.storage?.getItem('auth-user');
    if (!raw) return null;
    try {
      const user: unknown = JSON.parse(raw);
      if (typeof user !== 'object' || user === null) return null;
      const data = user as Record<string, unknown>;
      if (typeof data['token'] !== 'string' || !data['token'] ||
          typeof data['id'] !== 'string' || typeof data['correo'] !== 'string' ||
          typeof data['nombre'] !== 'string' || typeof data['apellido'] !== 'string' ||
          typeof data['tokenType'] !== 'string' || typeof data['rol'] !== 'string' ||
          !esRol(data['rol'])) return null;
      return data as unknown as AuthResponse['data'];
    } catch {
      return null;
    }
  }
  obtenerToken(): string | null {
    return this.obtenerUsuario()?.token || this.storage?.getItem('token') || null;
  }
  obtenerRol(): Rol | null {
    const role = this.obtenerUsuario()?.rol || this.storage?.getItem('rol') || null;
    return esRol(role) ? role : null;
  }
  obtenerUsuarioId(): string | null {
    return this.obtenerUsuario()?.id || this.storage?.getItem('usuarioId') || null;
  }
  obtenerNombre(): string { return this.obtenerUsuario()?.nombre || ''; }

  // Compatibilidad con los nombres del proyecto de referencia.
  saveUser(user: AuthResponse['data']): void { this.guardarSesion(user); }
  getUser(): AuthResponse['data'] | null { return this.obtenerUsuario(); }
  getUsuario(): AuthResponse['data'] | null { return this.obtenerUsuario(); }
  getNombre(): string { return this.obtenerNombre(); }
  getRol(): Rol | null { return this.obtenerRol(); }
  getId(): string | null { return this.obtenerUsuarioId(); }
  getToken(): string | null { return this.obtenerToken(); }
  isLoggedIn(): boolean { return !!this.obtenerToken(); }
  clean(): void { this.limpiarSesion(); }
  limpiarSesion(): void {
    for (const key of ['auth-user', 'token', 'rol', 'usuarioId']) this.storage?.removeItem(key);
    this.cambios.next();
  }
}
