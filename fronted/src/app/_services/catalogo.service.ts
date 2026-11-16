import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map, of, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/auth.interface';
import { Producto } from '../models/producto.interface';
import { Categoria } from '../models/categoria.interface';
import { MarcaVehiculo } from '../models/marca-vehiculo.interface';
import { ModeloVehiculo } from '../models/modelo-vehiculo.interface';
import { CompatibilidadVehiculo } from '../models/compatibilidad-vehiculo.interface';
import { Proveedor } from '../models/proveedor.interface';

export interface CatalogStock { productoId: string; cantidad: number; }
export interface CatalogRelation { productoId: string; categoriaId: string; }
export interface CatalogData {
  productos: Producto[]; categorias: Categoria[]; marcas: MarcaVehiculo[];
  modelos: ModeloVehiculo[]; compatibilidades: CompatibilidadVehiculo[];
  proveedores: Proveedor[]; stocks: CatalogStock[]; relaciones: CatalogRelation[];
}

@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private http = inject(HttpClient);
  private lista<T>(path: string): Observable<T[]> {
    return this.http.get<ApiResponse<T[]>>(`${environment.apiUrl}/${path}`).pipe(map(res => {
      if (!res.success || !Array.isArray(res.data)) throw new Error(res.message || 'Respuesta inválida');
      return res.data;
    }));
  }
  cargar(): Observable<CatalogData> {
    return forkJoin({
      productos: this.lista<Producto>('productos'),
      categorias: this.lista<Categoria>('categorias'),
      marcas: this.lista<MarcaVehiculo>('marcas-vehiculos'),
      modelos: this.lista<ModeloVehiculo>('modelos-vehiculos'),
      compatibilidades: this.lista<CompatibilidadVehiculo>('compatibilidades-vehiculos'),
      proveedores: this.lista<Proveedor>('proveedores'),
      stocks: this.lista<CatalogStock>('stocks'),
    }).pipe(switchMap(data => {
      const consultas = data.categorias.map(c => this.lista<CatalogRelation>(`producto-categorias/categoria/${encodeURIComponent(c.id)}`));
      return (consultas.length ? forkJoin(consultas) : of([] as CatalogRelation[][])).pipe(
        map(relaciones => ({ ...data, relaciones: relaciones.flat() }))
      );
    }));
  }
}
