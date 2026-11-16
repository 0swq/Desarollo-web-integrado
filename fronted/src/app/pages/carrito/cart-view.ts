import { ParametrosService } from '../../_services/parametros.service';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, catchError, finalize, forkJoin, map, of, switchMap, throwError } from 'rxjs';
import { toast } from 'ngx-sonner';
import { CarritoService } from '../../_services/carrito.service';
import { ProductoService } from '../../_services/producto.service';
import { CarritoResponse, ItemCarritoResponse } from '../../models/orden.interface';
import { Producto } from '../../models/producto.interface';
import { StorageService } from '../../_services/storage.service';


export interface CartLine extends ItemCarritoResponse { producto: Producto | null; imagenFallida: boolean; }
interface CartData { carrito: CarritoResponse; filas: CartLine[]; }

@Component({
  selector: 'app-cart-view', standalone: true, imports: [CommonModule, RouterLink],
  templateUrl: './cart-view.html', styleUrls: ['./cart-view.css'],
})
export class CartViewComponent implements OnInit {
  private carritoService = inject(CarritoService);
  private productos = inject(ProductoService);
  private storage = inject(StorageService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private parametros = inject(ParametrosService);
  get contacto() { return this.parametros.contacto(); }
  carrito: CarritoResponse | null = null;
  filas: CartLine[] = [];
  cargando = true;
  ocupado = false;
  filaPendiente = '';
  error = '';
  get hayProductosNoDisponibles() { return this.filas.some(f => !f.producto?.activo); }
  ngOnInit(): void { this.cargar(); }
  private consultar(): Observable<CartData> {
    const usuario = this.storage.obtenerUsuarioId();
    if (!usuario) return throwError(() => new Error('Inicia sesión para consultar tu carrito.'));
    return this.carritoService.obtenerCarrito(usuario).pipe(switchMap(carrito => {
      if (!carrito.items.length) return of({ carrito, filas: [] as CartLine[] });
      return forkJoin(carrito.items.map(item => this.productos.obtenerProducto(item.productoId).pipe(
        catchError(() => of(null)), map(producto => ({ ...item, producto, imagenFallida: false }))
      ))).pipe(map(filas => ({ carrito, filas })));
    }));
  }
  private mostrar(data: CartData): void { this.carrito = data.carrito; this.filas = data.filas; }
  cargar(): void {
    if (!this.storage.obtenerUsuarioId()) {
      this.cargando = false; void this.router.navigate(['/iniciar-sesion'], { queryParams: { returnUrl: '/carrito' } }); return;
    }
    this.cargando = true; this.error = '';
    this.consultar().pipe(finalize(() => this.cargando = false), takeUntilDestroyed(this.destroyRef)).subscribe({
      next: data => this.mostrar(data),
      error: () => this.error = 'No pudimos cargar tu carrito. Comprueba tu conexión y vuelve a intentarlo.',
    });
  }
  private mutar(request: Observable<unknown>, mensaje: string, fila = ''): void {
    this.ocupado = true; this.filaPendiente = fila;
    request.pipe(switchMap(() => this.consultar()), finalize(() => { this.ocupado = false; this.filaPendiente = ''; }), takeUntilDestroyed(this.destroyRef)).subscribe({
      next: data => { this.mostrar(data); toast.success(mensaje); },
      error: err => toast.error(err.error?.message || err.message || 'No se pudo actualizar el carrito.'),
    });
  }
  cambiarCantidad(item: CartLine, cantidad: number): void {
    if (!this.carrito || this.ocupado || !Number.isInteger(cantidad) || cantidad < 1 || cantidad === item.cantidad) return;
    this.mutar(this.carritoService.actualizarCantidad(this.carrito.id, item.id, cantidad), 'Cantidad actualizada.', item.id);
  }
  cantidadEscrita(item: CartLine, event: Event): void {
    const input = event.target as HTMLInputElement; const cantidad = Number(input.value); input.value = String(item.cantidad);
    if (!Number.isInteger(cantidad) || cantidad < 1) { toast.error('Ingresa una cantidad entera mayor que cero.'); return; }
    this.cambiarCantidad(item, cantidad);
  }
  eliminar(item: CartLine): void {
    if (!this.carrito || this.ocupado) return;
    this.mutar(this.carritoService.eliminarItem(this.carrito.id, item.id), 'Producto eliminado del carrito.', item.id);
  }
  vaciar(): void {
    const usuario = this.storage.obtenerUsuarioId();
    if (!usuario || this.ocupado || !this.filas.length) return;
    this.mutar(this.carritoService.vaciarCarrito(usuario), 'Carrito vaciado.');
  }
  realizarCheckout(): void {
    if (this.filas.length && !this.ocupado && !this.hayProductosNoDisponibles) void this.router.navigate(['/pago/llenar-datos']);
  }
}
