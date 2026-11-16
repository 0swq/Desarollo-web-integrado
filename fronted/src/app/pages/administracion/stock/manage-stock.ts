import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { toast } from 'ngx-sonner';
import { catchError, finalize, forkJoin, of, Subscription } from 'rxjs';
import { AdminCatalogoService } from '../../../_services/admin-catalogo.service';
import { ConfiguracionStock, StockService } from '../../../_services/stock';
import { Producto } from '../../../models/producto.interface';
import { Stock } from '../../../models/stock.interface';

type FiltroStock = 'todos' | 'bajo' | 'agotado' | 'disponible' | 'sin-minimo';
type EstadoStock = Exclude<FiltroStock, 'todos'>;
type AccionStock = 'detalle' | 'editar';

const NOMBRES_ESTADO: Record<EstadoStock, string> = {
  bajo: 'En el mínimo o por debajo',
  agotado: 'Agotado',
  disponible: 'Disponible',
  'sin-minimo': 'Sin mínimo',
};

@Component({
  selector: 'app-manage-stock',
  imports: [DatePipe, FormsModule, ReactiveFormsModule],
  templateUrl: './manage-stock.html',
  styleUrl: './manage-stock.css',
})
export class ManageStock implements OnInit {
  private readonly api = inject(StockService);
  private readonly catalogo = inject(AdminCatalogoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');
  private consultaDetalle?: Subscription;

  private readonly stocks = signal<Stock[]>([]);
  private readonly productos = signal<Producto[]>([]);
  private readonly productosPorId = computed(() => new Map(this.productos().map(producto => [producto.id, producto])));
  private readonly alertas = signal<Stock[] | null>(null);
  protected readonly busqueda = signal('');
  protected readonly filtro = signal<FiltroStock>('todos');
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  protected readonly nombresEstado = NOMBRES_ESTADO;
  protected readonly total = computed(() => this.stocks().length);
  protected readonly agotados = computed(() => this.stocks().filter(stock => stock.cantidad === 0).length);
  protected readonly stocksBajos = computed(() => this.alertas() ?? this.stocks().filter(esBajoStock));

  protected readonly visibles = computed(() => {
    const filtro = this.filtro();
    const busqueda = normalizar(this.busqueda().trim());
    const stocks = filtro === 'bajo' ? this.stocksBajos() : this.stocks();
    return stocks.map(stock => ({ stock, producto: this.productoDe(stock), estado: estadoDe(stock) }))
      .filter(fila => (filtro === 'todos' || filtro === 'bajo'
        || (filtro === 'sin-minimo' ? fila.stock.stockMinimo == null : fila.estado === filtro))
        && (!busqueda || normalizar([
          fila.producto?.nombre, fila.producto?.sku, fila.stock.ubicacionAlmacen,
        ].join(' ')).includes(busqueda)))
      .sort((a, b) => (a.producto?.nombre ?? '').localeCompare(b.producto?.nombre ?? '', 'es'));
  });

  protected readonly accion = signal<AccionStock>('detalle');
  protected readonly seleccionado = signal<Stock | null>(null);
  protected readonly detalle = signal<Stock | null>(null);
  protected readonly errorDetalle = signal('');
  protected readonly errorAccion = signal('');
  protected readonly guardando = signal(false);

  protected readonly formulario = inject(FormBuilder).nonNullable.group({
    stockMinimo: new FormControl<number | null>(null),
    ubicacionAlmacen: '',
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    if (this.cargando() || this.guardando()) return;

    this.cargando.set(true);
    this.error.set('');
    forkJoin({
      stocks: this.api.listar(),
      productos: this.catalogo.listar<Producto>('productos'),
      alertas: this.api.bajoStock().pipe(catchError(() => of(null))),
    }).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.cargando.set(false)),
    ).subscribe({
      next: ({ stocks, productos, alertas }) => {
        this.stocks.set(stocks);
        this.productos.set(productos);
        this.alertas.set(alertas);
      },
      error: error => this.error.set(mensajeError(error, 'No se pudo cargar el inventario.')),
    });
  }

  protected productoDe(stock: Stock): Producto | undefined {
    return this.productosPorId().get(stock.productoId ?? stock.producto?.id ?? '') ?? stock.producto;
  }

  protected abrir(accion: AccionStock, stock: Stock): void {
    if (accion === 'editar' && !stock.productoId) return;

    this.accion.set(accion);
    this.seleccionado.set(stock);
    this.cargarDetalle();
    this.dialogo().nativeElement.showModal();
  }

  protected cargarDetalle(): void {
    const stock = this.seleccionado();
    if (!stock) return;

    this.consultaDetalle?.unsubscribe();
    this.detalle.set(null);
    this.errorDetalle.set('');
    this.errorAccion.set('');
    const solicitud = this.accion() === 'editar' && stock.productoId
      ? this.api.porProducto(stock.productoId)
      : this.api.obtener(stock.id);
    this.consultaDetalle = solicitud.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: detalle => {
        this.detalle.set(detalle);
        this.formulario.reset({
          stockMinimo: detalle.stockMinimo ?? null,
          ubicacionAlmacen: detalle.ubicacionAlmacen ?? '',
        });
        this.actualizarFila(detalle);
      },
      error: error => this.errorDetalle.set(mensajeError(error, 'No se pudo obtener el detalle de stock.')),
    });
  }

  protected cerrar(evento?: Event): void {
    evento?.preventDefault();
    if (this.guardando()) return;

    this.consultaDetalle?.unsubscribe();
    this.dialogo().nativeElement.close();
  }

  protected guardar(): void {
    const detalle = this.detalle();
    if (this.accion() !== 'editar' || this.guardando() || !detalle?.productoId) return;

    const { stockMinimo, ubicacionAlmacen } = this.formulario.getRawValue();
    const ubicacion = ubicacionAlmacen.trim();
    if (stockMinimo != null && (!Number.isInteger(stockMinimo) || stockMinimo < 0 || stockMinimo > 2147483647)) {
      this.errorAccion.set('Ingresa un stock mínimo entero entre 0 y 2 147 483 647.');
      return;
    }
    if (ubicacion.length > 100) {
      this.errorAccion.set('La ubicación puede tener hasta 100 caracteres.');
      return;
    }

    const cambios: ConfiguracionStock = {};
    if (stockMinimo != null && stockMinimo !== detalle.stockMinimo) cambios.stockMinimo = stockMinimo;
    if (ubicacion !== (detalle.ubicacionAlmacen ?? '').trim()) cambios.ubicacionAlmacen = ubicacion;
    if (Object.keys(cambios).length === 0) {
      this.errorAccion.set('Ingresa un cambio antes de guardar.');
      return;
    }

    this.guardando.set(true);
    this.errorAccion.set('');
    this.api.actualizarConfiguracion(detalle.productoId, cambios).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.guardando.set(false)),
    ).subscribe({
      next: actualizado => {
        this.actualizarFila(actualizado);
        this.dialogo().nativeElement.close();
        toast.success('Configuración de stock actualizada.');
      },
      error: error => this.errorAccion.set(mensajeError(error, 'No se pudo guardar la configuración de stock.')),
    });
  }

  private actualizarFila(stock: Stock): void {
    this.stocks.update(stocks => stocks.map(fila => fila.id === stock.id ? stock : fila));
    this.alertas.update(alertas => {
      if (alertas == null) return null;
      const restantes = alertas.filter(fila => fila.id !== stock.id);
      return esBajoStock(stock) ? [...restantes, stock] : restantes;
    });
  }
}

function esBajoStock(stock: Stock): boolean {
  return stock.stockMinimo != null && stock.cantidad <= stock.stockMinimo;
}

function estadoDe(stock: Stock): EstadoStock {
  if (stock.cantidad === 0) return 'agotado';
  if (stock.stockMinimo == null) return 'sin-minimo';
  return esBajoStock(stock) ? 'bajo' : 'disponible';
}

function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase();
}

function mensajeError(error: unknown, mensaje: string): string {
  if (error instanceof HttpErrorResponse) {
    if (typeof error.error?.message === 'string') return error.error.message;
    if (error.status === 403) return 'Tu cuenta no tiene permiso para realizar esta acción.';
    return mensaje;
  }
  return error instanceof Error ? error.message : mensaje;
}
