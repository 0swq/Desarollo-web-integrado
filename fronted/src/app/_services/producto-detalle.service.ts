import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/auth.interface';
import { Producto } from '../models/producto.interface';
import { Categoria } from '../models/categoria.interface';
import { MarcaVehiculo } from '../models/marca-vehiculo.interface';
import { ModeloVehiculo } from '../models/modelo-vehiculo.interface';
import { CompatibilidadVehiculo } from '../models/compatibilidad-vehiculo.interface';
import { CatalogRelation, CatalogStock } from './catalogo.service';
import { ProductoService } from './producto.service';

export interface ProductoDetalleData {
  producto: Producto;
  stock: CatalogStock | null;
  categorias: Categoria[];
  compatibilidades: CompatibilidadVehiculo[];
  modelos: ModeloVehiculo[];
  marcas: MarcaVehiculo[];
  otros: Producto[];
  datosIncompletos: boolean;
}

@Injectable({ providedIn: 'root' })
export class ProductoDetalleService {
  private http = inject(HttpClient);
  private productos = inject(ProductoService);
  private opcional<T>(path: string, fallback: T): Observable<{ data: T; error: boolean }> {
    return this.http.get<ApiResponse<T>>(`${environment.apiUrl}/${path}`).pipe(
      map(res => { if (!res.success || res.data == null) throw new Error(); return { data: res.data, error: false }; }),
      catchError(() => of({ data: fallback, error: true }))
    );
  }
  obtener(id: string): Observable<ProductoDetalleData> {
    const safeId = encodeURIComponent(id);
    return forkJoin({
      producto: this.productos.obtenerProducto(id),
      stock: this.opcional<CatalogStock[]>('stocks', []),
      categorias: this.opcional<Categoria[]>('categorias', []),
      relaciones: this.opcional<CatalogRelation[]>(`producto-categorias/producto/${safeId}`, []),
      compatibilidades: this.opcional<CompatibilidadVehiculo[]>(`compatibilidades-vehiculos/producto/${safeId}`, []),
      modelos: this.opcional<ModeloVehiculo[]>('modelos-vehiculos', []),
      marcas: this.opcional<MarcaVehiculo[]>('marcas-vehiculos', []),
      otros: this.opcional<Producto[]>('productos', []),
    }).pipe(map(res => ({
      producto: res.producto, stock: res.stock.data.find(s => s.productoId === res.producto.id) ?? null,
      categorias: res.categorias.data.filter(c => res.relaciones.data.some(r => r.categoriaId === c.id)),
      compatibilidades: res.compatibilidades.data, modelos: res.modelos.data, marcas: res.marcas.data,
      otros: res.otros.data.filter(p => p.activo && p.id !== res.producto.id).slice(0, 4),
      datosIncompletos: [res.stock, res.categorias, res.relaciones, res.compatibilidades, res.modelos, res.marcas].some(r => r.error),
    })));
  }
}
