import { environment } from '../../environments/environment';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject, map, tap } from 'rxjs';
import { CarritoResponse, ItemCarritoResponse } from '../models/orden.interface';
import { ApiResponse } from '../models/auth.interface';

@Injectable({
  providedIn: 'root',
})
export class CarritoService {
  private cambios = new Subject<void>();
  readonly cambios$ = this.cambios.asObservable();
  notificarCambio(): void {
    this.cambios.next();
  }
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/carritos`;

                                                  
  obtenerCarrito(usuarioId: string): Observable<CarritoResponse> {
    return this.http
      .get<ApiResponse<CarritoResponse>>(`${this.apiUrl}/usuario/${encodeURIComponent(usuarioId)}`)
      .pipe(
        map((res) => {
          if (!res.success || !res.data)
            throw new Error(res.message || 'No se pudo obtener el carrito.');
          return res.data;
        }),
      );
  }

                              
  agregarItem(
    carritoId: string,
    productoId: string,
    cantidad: number,
  ): Observable<ApiResponse<ItemCarritoResponse>> {
    return this.http
      .post<ApiResponse<ItemCarritoResponse>>(
        `${this.apiUrl}/${encodeURIComponent(carritoId)}/items`,
        { productoId, cantidad },
      )
      .pipe(
        tap((res) => {
          if (res.success) this.cambios.next();
        }),
      );
  }

  actualizarCantidad(
    carritoId: string,
    itemId: string,
    cantidad: number,
  ): Observable<ItemCarritoResponse> {
    return this.http
      .put<ApiResponse<ItemCarritoResponse>>(
        `${this.apiUrl}/${encodeURIComponent(carritoId)}/items/${encodeURIComponent(itemId)}`,
        { cantidad },
      )
      .pipe(
        map((res) => {
          if (!res.success || !res.data)
            throw new Error(res.message || 'No se pudo actualizar la cantidad.');
          return res.data;
        }),
        tap(() => this.cambios.next()),
      );
  }

  eliminarItem(carritoId: string, itemId: string): Observable<void> {
    return this.http
      .delete<void>(
        `${this.apiUrl}/${encodeURIComponent(carritoId)}/items/${encodeURIComponent(itemId)}`,
      )
      .pipe(tap(() => this.cambios.next()));
  }

                   
  vaciarCarrito(usuarioId: string): Observable<void> {
    return this.http
      .delete<void>(`${this.apiUrl}/usuario/${encodeURIComponent(usuarioId)}`)
      .pipe(tap(() => this.cambios.next()));
  }
}
