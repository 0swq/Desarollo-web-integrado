import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/auth.interface';
import { EstadoOrden } from '../models/estado-orden';
import { Orden } from '../models/orden.interface';

@Injectable({
  providedIn: 'root',
})
export class OrdenService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/ordenes`;

  listar(): Observable<Orden[]> {
    return this.http.get<ApiResponse<Orden[]>>(this.url).pipe(map(respuesta => {
      if (!respuesta.success || !Array.isArray(respuesta.data)) {
        throw new Error(respuesta.message || 'No se pudo leer el listado de pedidos.');
      }
      return respuesta.data;
    }));
  }

  obtener(id: string): Observable<Orden> {
    return this.http.get<ApiResponse<Orden>>(`${this.url}/${encodeURIComponent(id)}`).pipe(map(datoOrden));
  }

  actualizarEstado(id: string, estado: EstadoOrden): Observable<Orden> {
    return this.http.patch<ApiResponse<Orden>>(`${this.url}/${encodeURIComponent(id)}/estado`, { estado })
      .pipe(map(datoOrden));
  }
}

function datoOrden(respuesta: ApiResponse<Orden>): Orden {
  if (!respuesta.success || !respuesta.data) {
    throw new Error(respuesta.message || 'No se pudo obtener el pedido.');
  }
  return respuesta.data;
}
