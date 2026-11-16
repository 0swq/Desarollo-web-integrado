import { environment } from '../../environments/environment';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiResponse, LoginRequest, AuthResponse, RegisterRequest, UsuarioResponse } from '../models/auth.interface';
import { StorageService } from './storage.service';
import { ROLES, ROLES_GESTION, ROLES_USUARIOS, Rol } from '../models/rol';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private storage = inject(StorageService);
  private apiUrl = `${environment.apiUrl}/auth`;

  login(credenciales: LoginRequest): Observable<ApiResponse<AuthResponse['data']>> {
    return this.http.post<ApiResponse<AuthResponse['data']>>(`${this.apiUrl}/login`, credenciales).pipe(
      tap(response => {
        if (response.success && response.data?.token) {
          this.storage.guardarSesion(response.data);
        }
      })
    );
  }

  registrar(datos: RegisterRequest): Observable<ApiResponse<UsuarioResponse>> {
    return this.http.post<ApiResponse<UsuarioResponse>>(`${this.apiUrl}/register`, datos);
  }

  register(datos: RegisterRequest): Observable<ApiResponse<UsuarioResponse>> {
    return this.registrar(datos);
  }

  obtenerUsuario(): AuthResponse['data'] | null {
    return this.storage.obtenerUsuario();
  }

  logout(): void {
    this.storage.limpiarSesion();
  }

  estaAutenticado(): boolean {
    return !!this.storage.obtenerToken();
  }

  // Nuevos métodos para verificar roles fácilmente
  obtenerRol(): Rol | null {
    return this.storage.obtenerRol();
  }

  esAdmin(): boolean {
    return this.obtenerRol() === ROLES.ADMIN;
  }

  esCliente(): boolean {
    return this.obtenerRol() === ROLES.CLIENTE;
  }

  esEmpleado(): boolean {
    return this.obtenerRol() === ROLES.EMPLEADO;
  }

  tieneRol(roles: readonly Rol[]): boolean {
    const role = this.obtenerRol();
    return this.estaAutenticado() && role !== null && roles.includes(role);
  }

  puedeGestionar(): boolean {
    return this.tieneRol(ROLES_GESTION);
  }

  puedeGestionarUsuarios(): boolean {
    return this.tieneRol(ROLES_USUARIOS);
  }
}
