import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map, switchMap, of, catchError, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/auth.interface';
import { Orden } from '../models/orden.interface';
import { PedidoItem } from './perfil.service';
import { CarritoService } from './carrito.service';
import { ProductoService } from './producto.service';
import { StorageService } from './storage.service';
export interface Entrega { direccionEntrega: string; telefonoContacto: string; notas: string; }
export interface CompraResumen { orden: Orden; pago: { id: string; estado: 'PENDIENTE' | 'APROBADO' | 'RECHAZADO' | 'REEMBOLSADO'; metodoPago: string; monto: number; moneda: string }; items: PedidoItem[]; }
export interface LineaCompra { nombreProducto: string; sku: string; cantidad: number; subtotal: number; imagenUrl?: string | null; }
export interface VistaCarrito { items: LineaCompra[]; subtotal: number; igv: number; total: number; moneda: string; }
@Injectable({ providedIn: 'root' })
export class CheckoutService {
 private http=inject(HttpClient); private cart=inject(CarritoService); private productos=inject(ProductoService); private storage=inject(StorageService); private platform=inject(PLATFORM_ID);
 private base=`${environment.apiUrl}/checkout`;
 private key(tipo: string) { return `checkout-${this.storage.obtenerUsuarioId()}-${tipo}`; }
 private leer(tipo: string): string | null { try { return isPlatformBrowser(this.platform) ? sessionStorage.getItem(this.key(tipo)) : null; } catch { return null; } }
 private escribir(tipo: string, valor: string): void { try { if (isPlatformBrowser(this.platform)) sessionStorage.setItem(this.key(tipo),valor); } catch {} }
 guardarEntrega(datos: Entrega): void { this.escribir('entrega',JSON.stringify(datos)); }
 entrega(): Entrega | null { try { const d=JSON.parse(this.leer('entrega') || 'null'); return d && typeof d.direccionEntrega==='string' && typeof d.telefonoContacto==='string' && typeof d.notas==='string' ? d : null; } catch { return null; } }
 pedidoActual(): string | null { return this.leer('pedido'); }
 private datos<T>(r: ApiResponse<T>): T { if (!r.success || !r.data) throw new Error(r.message || 'No pudimos completar la operación.'); return r.data; }
 carrito(): Observable<VistaCarrito> {
  return this.cart.obtenerCarrito(this.storage.obtenerUsuarioId() || '').pipe(switchMap(c => {
   const items=c.items.map(i => this.productos.obtenerProducto(i.productoId).pipe(map(p => { if (!p.activo) throw new Error('Uno de los productos ya no está disponible. Revisa tu carrito.'); return { nombreProducto:p.nombre,sku:p.sku,cantidad:i.cantidad,subtotal:i.subtotal,imagenUrl:p.imagenUrl }; })));
   return (items.length ? forkJoin(items) : of([])).pipe(map(items => ({ items,subtotal:c.total,igv:Math.round(c.total*18)/100,total:Math.round((c.total+Math.round(c.total*18)/100)*100)/100,moneda:c.moneda || 'PEN' })));
  }));
 }
 crear(datos: Entrega): Observable<CompraResumen> { return this.http.post<ApiResponse<CompraResumen>>(`${this.base}/ordenes`,datos).pipe(map(r=>this.datos(r)),tap(r=>{ this.escribir('pedido',r.orden.id); this.cart.notificarCambio(); })); }
 obtener(id: string): Observable<CompraResumen> { return this.http.get<ApiResponse<CompraResumen>>(`${this.base}/ordenes/${encodeURIComponent(id)}`).pipe(map(r=>this.datos(r))); }
 simular(id: string,resultado: 'APROBADO' | 'RECHAZADO'): Observable<CompraResumen> { return this.http.post<ApiResponse<CompraResumen>>(`${this.base}/ordenes/${encodeURIComponent(id)}/simular`,{resultado}).pipe(map(r=>this.datos(r))); }
}
