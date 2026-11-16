import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/auth.interface';

type RecursoCatalogo = 'marcas-vehiculos' | 'modelos-vehiculos' | 'categorias' | 'productos' | 'proveedores';

@Injectable({ providedIn: 'root' })
export class AdminCatalogoService {
  private http = inject(HttpClient);

  imagenPublica(url?: string | null): string {
    if (!url || /^(https?:|blob:|data:)/i.test(url)) return url || '';
    if (url.startsWith('/api/')) {
      const origen = environment.apiUrl.replace(/\/api\/?$/, '');
      return origen ? `${origen}${url}` : url;
    }
    return url;
  }

  listar<T>(recurso: RecursoCatalogo): Observable<T[]> {
    return this.leerLista<T>(recurso);
  }

  categoriasDeProducto(id: string): Observable<{ categoriaId: string }[]> {
    return this.leerLista(`producto-categorias/producto/${encodeURIComponent(id)}`);
  }

  private leerLista<T>(recurso: string): Observable<T[]> {
    return this.http.get<ApiResponse<T[]>>(`${environment.apiUrl}/${recurso}`).pipe(
      map(respuesta => {
        if (!respuesta.success || !Array.isArray(respuesta.data)) {
          throw new Error(respuesta.message || 'No se pudo leer el catálogo.');
        }
        return respuesta.data;
      }),
    );
  }

  guardar<T>(recurso: RecursoCatalogo, datos: object, id?: string): Observable<T> {
    const solicitud = id
      ? this.http.put<ApiResponse<T>>(`${environment.apiUrl}/${recurso}/${encodeURIComponent(id)}`, datos)
      : this.http.post<ApiResponse<T>>(`${environment.apiUrl}/${recurso}`, datos);
    return solicitud.pipe(map(respuesta => {
      if (!respuesta.success || respuesta.data == null) throw new Error(respuesta.message || 'No se pudo guardar el registro.');
      return respuesta.data;
    }));
  }

  eliminar(recurso: 'productos' | 'proveedores', id: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/${recurso}/${encodeURIComponent(id)}`);
  }

  cambiarEstado<T>(recurso: Exclude<RecursoCatalogo, 'productos'>, id: string): Observable<T> {
    return this.http.patch<ApiResponse<T>>(`${environment.apiUrl}/${recurso}/${encodeURIComponent(id)}/toggle-activo`, {}).pipe(map(respuesta => {
      if (!respuesta.success || respuesta.data == null) throw new Error(respuesta.message || 'No se pudo cambiar el estado.');
      return respuesta.data;
    }));
  }

  subirImagen(archivo: File, carpeta: 'marcas' | 'modelos' | 'categorias' | 'proveedores'): Observable<{ ruta: string; url: string }> {
    const datos = new FormData(); datos.append('archivo', archivo);
    return this.http.post<ApiResponse<{ ruta: string; url: string }>>(`${environment.apiUrl}/storage/imagenes?carpeta=${carpeta}`, datos).pipe(map(respuesta => {
      if (!respuesta.success || !respuesta.data) throw new Error(respuesta.message || 'No se pudo subir la imagen.');
      return respuesta.data;
    }));
  }
}
