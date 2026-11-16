import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/auth.interface';
import { Stock } from '../models/stock.interface';

export interface ConfiguracionStock {
  stockMinimo?: number;
  ubicacionAlmacen?: string;
}

@Injectable({
  providedIn: 'root',
})
export class StockService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/stocks`;

  listar(): Observable<Stock[]> {
    return this.leerLista(this.url);
  }

  bajoStock(): Observable<Stock[]> {
    return this.leerLista(`${this.url}/bajo-stock`);
  }

  obtener(id: string): Observable<Stock> {
    return this.http.get<ApiResponse<Stock>>(`${this.url}/${encodeURIComponent(id)}`).pipe(map(leerStock));
  }

  porProducto(productoId: string): Observable<Stock> {
    return this.http.get<ApiResponse<Stock>>(this.urlProducto(productoId)).pipe(map(leerStock));
  }

  actualizarConfiguracion(productoId: string, datos: ConfiguracionStock): Observable<Stock> {
    return this.http.put<ApiResponse<Stock>>(this.urlProducto(productoId), datos).pipe(map(leerStock));
  }

  private urlProducto(productoId: string): string {
    return `${this.url}/producto/${encodeURIComponent(productoId)}`;
  }

  private leerLista(url: string): Observable<Stock[]> {
    return this.http.get<ApiResponse<Stock[]>>(url).pipe(map(respuesta => {
      if (!respuesta.success || !Array.isArray(respuesta.data)) {
        throw new Error(respuesta.message || 'No se pudo leer el inventario.');
      }
      return respuesta.data;
    }));
  }
}

function leerStock(respuesta: ApiResponse<Stock>): Stock {
  if (!respuesta.success || respuesta.data == null) {
    throw new Error(respuesta.message || 'No se pudo obtener el registro de stock.');
  }
  return respuesta.data;
}
