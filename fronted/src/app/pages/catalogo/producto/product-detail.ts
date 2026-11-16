import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, Subscription, catchError, combineLatest, distinctUntilChanged, finalize, map, of, startWith, switchMap } from 'rxjs';
import { toast } from 'ngx-sonner';
import { ProductoDetalleData, ProductoDetalleService } from '../../../_services/producto-detalle.service';
import { CarritoService } from '../../../_services/carrito.service';
import { StorageService } from '../../../_services/storage.service';
import { CompatibilidadVehiculo } from '../../../models/compatibilidad-vehiculo.interface';
import { ProductCard } from '../../../_shared/componentes/product-card/product-card';
import { CompatibilityCard } from '../../../_shared/componentes/compatibility-card/compatibility-card';

@Component({
  selector: 'app-product-detail', standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ProductCard, CompatibilityCard],
  templateUrl: './product-detail.html', styleUrl: './product-detail.css',
})
export class ProductDetail {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private service = inject(ProductoDetalleService);
  private cart = inject(CarritoService);
  private storage = inject(StorageService);
  private destroyRef = inject(DestroyRef);
  private compra?: Subscription;
  private recargar = new Subject<void>();
  detalle: ProductoDetalleData | null = null;
  cargando = true;
  error = '';
  noEncontrado = false;
  cantidad = 1;
  agregando = false;
  agregado = false;
  imagenFallida = false;
  tab: 'descripcion' | 'ficha' | 'compatibilidad' = 'descripcion';
  constructor() {
    this.destroyRef.onDestroy(() => this.compra?.unsubscribe());
    combineLatest([
      this.route.paramMap.pipe(map(params => params.get('id') || ''), distinctUntilChanged()),
      this.recargar.pipe(startWith(undefined)),
    ]).pipe(
      switchMap(([id]) => {
        this.compra?.unsubscribe();
        this.cargando = true; this.detalle = null; this.error = ''; this.noEncontrado = false;
        this.cantidad = 1; this.agregando = false; this.agregado = false; this.imagenFallida = false; this.tab = 'descripcion';
        return this.service.obtener(id).pipe(
          map(detalle => ({ detalle, error: '', noEncontrado: !detalle.producto.activo })),
          catchError(error => of({ detalle: null, noEncontrado: error.status === 404 || error.status === 400,
            error: 'No pudimos cargar este producto. Comprueba tu conexión y vuelve a intentarlo.' }))
        );
      }), takeUntilDestroyed()
    ).subscribe(state => {
      this.detalle = state.detalle; this.error = state.error; this.noEncontrado = state.noEncontrado; this.cargando = false;
    });
  }
  get producto() { return this.detalle?.producto; }
  get stock(): number | null { return this.detalle?.stock?.cantidad ?? null; }
  get disponible() { return !!this.producto?.activo && this.stock !== null && this.stock > 0; }
  get enOferta() { return this.producto?.precioPromocional != null && this.producto.precioPromocional > 0 && this.producto.precioPromocional < this.producto.precio; }
  get precio() { return this.enOferta ? this.producto!.precioPromocional! : this.producto?.precio || 0; }
  get descuento() { return this.enOferta ? Math.round((1 - this.precio / this.producto!.precio) * 100) : 0; }
  get categorias() { return this.detalle?.categorias.map(c => c.nombre).join(' · ') || 'Repuestos y accesorios'; }
  get cantidadValida() { return Number.isInteger(this.cantidad) && this.cantidad >= 1 && this.cantidad <= (this.stock ?? 0); }
  get haySesion() { return !!this.storage.obtenerUsuarioId() && !!this.storage.obtenerToken(); }
  normalizarCantidad() { this.cantidad = Math.max(1, Math.min(Math.floor(Number(this.cantidad) || 1), Math.max(1, this.stock ?? 1))); }
  modelo(c: CompatibilidadVehiculo) { return this.detalle?.modelos.find(m => m.id === c.modeloVehiculoId); }
  marca(c: CompatibilidadVehiculo) { return this.detalle?.marcas.find(m => m.id === this.modelo(c)?.marcaId)?.nombre || 'Sin especificar'; }
  anios(c: CompatibilidadVehiculo) {
    if (c.anioInicio == null && c.anioFin == null) return 'Sin especificar';
    return `${c.anioInicio ?? 'Sin inicio'} – ${c.anioFin ?? 'En adelante'}`;
  }
  reintentar() { this.recargar.next(); }
  iniciarSesion() { void this.router.navigate(['/iniciar-sesion'], { queryParams: { returnUrl: this.router.url } }); }
  agregar(): void {
    if (!this.disponible || this.agregando || !this.cantidadValida || !this.producto) return;
    const usuario = this.storage.obtenerUsuarioId();
    if (!usuario || !this.haySesion) { this.iniciarSesion(); return; }
    const producto = this.producto;
    this.agregando = true; this.agregado = false;
    this.compra = this.cart.obtenerCarrito(usuario).pipe(
      switchMap(carrito => this.cart.agregarItem(carrito.id, producto.id, this.cantidad)),
      finalize(() => this.agregando = false), takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: res => {
        if (!res.success) { toast.error(res.message || 'No se pudo añadir el producto.'); return; }
        this.agregado = true; toast.success('Producto añadido al carrito.');
      },
      error: err => toast.error(err.error?.message || err.message || 'No se pudo añadir el producto al carrito.'),
    });
  }
}
