import { environment } from '../../environments/environment';
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Producto } from '../models/producto.interface';
import { ApiResponse } from '../models/auth.interface';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private http = inject(HttpClient);
  // URL de la API de tu amigo para los productos
  private apiUrl = `${environment.apiUrl}/productos`;

  obtenerProductos(): Observable<Producto[]> {
    return this.http.get<ApiResponse<Producto[]>>(this.apiUrl).pipe(map(response => response.data ?? []));
  }

  obtenerProducto(id: string): Observable<Producto> {
    return this.http.get<ApiResponse<Producto>>(`${this.apiUrl}/${encodeURIComponent(id)}`).pipe(map(response => {
      if (!response.success || !response.data) throw new Error(response.message || 'Producto no disponible.');
      return response.data;
    }));
  }

  buscarProductos(termino: string): Observable<Producto[]> {
    return this.http.get<ApiResponse<Producto[]>>(`${this.apiUrl}/buscar`, {
      params: { q: termino.trim() },
    }).pipe(map(response => response.data ?? []));
  }
}
