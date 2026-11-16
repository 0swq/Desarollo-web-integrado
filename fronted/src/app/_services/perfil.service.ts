import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, AuthResponse, UsuarioResponse } from '../models/auth.interface';
import { Orden } from '../models/orden.interface';
import { StorageService } from './storage.service';
export interface PerfilDatos { nombre: string; apellido: string; telefono: string; direccion: string; fotoPerfilUrl: string; }
export interface PedidoItem { id: string; productoId: string | null; nombreProducto: string; sku: string; cantidad: number; precioUnitario: number; subtotal: number; }
@Injectable({ providedIn: 'root' })
export class PerfilService {
  private http = inject(HttpClient);
  private storage = inject(StorageService);
  private base = `${environment.apiUrl}/perfil`;
  private datos<T>(res: ApiResponse<T>): T { if (!res.success || res.data === null) throw new Error(res.message || 'No se pudieron obtener los datos.'); return res.data; }
  private sesion(usuario: UsuarioResponse): void {
    const actual = this.storage.obtenerUsuario();
    const token = this.storage.obtenerToken();
    if (!token || this.storage.obtenerUsuarioId() !== usuario.id) return;
    const actualizado: AuthResponse['data'] = { token, tokenType: actual?.tokenType || 'Bearer', id: usuario.id,
      correo: usuario.correo, nombre: usuario.nombre, apellido: usuario.apellido, rol: usuario.rol, fotoPerfilUrl: usuario.fotoPerfilUrl };
    if (!actual || (Object.keys(actualizado) as (keyof AuthResponse['data'])[]).some(key => actual[key] !== actualizado[key])) this.storage.guardarSesion(actualizado);
  }
  obtener(): Observable<UsuarioResponse> { return this.http.get<ApiResponse<UsuarioResponse>>(this.base).pipe(map(r => this.datos(r)), tap(u => this.sesion(u))); }
  guardar(datos: PerfilDatos): Observable<UsuarioResponse> { return this.http.put<ApiResponse<UsuarioResponse>>(this.base, datos).pipe(map(r => this.datos(r)), tap(u => this.sesion(u))); }
  subirFoto(archivo: File): Observable<UsuarioResponse> {
    const data = new FormData(); data.append('archivo', archivo);
    return this.http.post<ApiResponse<UsuarioResponse>>(`${this.base}/foto`, data).pipe(map(r => this.datos(r)), tap(u => this.sesion(u)));
  }
  cambiarPassword(datos: { contrasenaActual: string; contrasenaNueva: string; confirmacionContrasena: string }): Observable<void> {
    return this.http.patch<ApiResponse<void>>(`${this.base}/password`, datos).pipe(map(res => {
      if (!res.success) throw new Error(res.message || 'No se pudo cambiar la contraseña.');
    }));
  }
  cambiarCorreo(datos: { correoNuevo: string; confirmacionCorreo: string; contrasenaActual: string }): Observable<AuthResponse['data']> {
    return this.http.patch<ApiResponse<AuthResponse['data']>>(`${this.base}/correo`, datos).pipe(map(res => this.datos(res)), tap(sesion => this.storage.guardarSesion(sesion)));
  }
  pedidos(): Observable<Orden[]> { return this.http.get<ApiResponse<Orden[]>>(`${environment.apiUrl}/ordenes/usuario/${encodeURIComponent(this.storage.obtenerUsuarioId() || '')}`).pipe(map(r => this.datos(r))); }
  items(id: string): Observable<PedidoItem[]> { return this.http.get<ApiResponse<PedidoItem[]>>(`${this.base}/pedidos/${encodeURIComponent(id)}/items`).pipe(map(r => this.datos(r))); }
}
