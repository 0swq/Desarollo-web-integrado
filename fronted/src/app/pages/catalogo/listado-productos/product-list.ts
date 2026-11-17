import { ParametrosService } from '../../../_services/parametros.service';
import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CatalogData, CatalogoService } from '../../../_services/catalogo.service';
import { Producto } from '../../../models/producto.interface';
import { ProductCard } from '../../../_shared/componentes/product-card/product-card';
import {
  VehiclePicker,
  VehiclePickerOption,
} from '../../../_shared/componentes/vehicle-picker/vehicle-picker';

const vacios = () => ({
  q: '',
  sku: '',
  categoria: '',
  proveedor: '',
  marca: '',
  modelo: '',
  anio: '',
  motor: '',
  tipo: '',
  minimo: '',
  maximo: '',
  moneda: '',
  stock: false,
  oferta: false,
});

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ProductCard, RouterLink, VehiclePicker],
  templateUrl: './product-list.html',
  styleUrls: ['./product-list.css'],
})
export class ProductListComponent {
  readonly parametros = inject(ParametrosService);
  private service = inject(CatalogoService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private filtroPendiente?: ReturnType<typeof setTimeout>;

  data: CatalogData = {
    productos: [],
    categorias: [],
    marcas: [],
    modelos: [],
    compatibilidades: [],
    proveedores: [],
    stocks: [],
    relaciones: [],
  };

  filtros = vacios();
  resultados: Producto[] = [];
  cargando = true;
  error = '';
  filtrosAbiertos = false;
  orden = 'nombre';
  pagina = 1;
  porPagina = 12;

  constructor() {
    this.destroyRef.onDestroy(() => clearTimeout(this.filtroPendiente));
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      clearTimeout(this.filtroPendiente);
      const defaults = vacios();
      for (const key of Object.keys(defaults) as (keyof typeof defaults)[]) {
        if (key === 'stock' || key === 'oferta') defaults[key] = params.get(key) === 'true';
        else defaults[key] = params.get(key) || '';
      }
      this.filtros = defaults;
      this.orden = ['nombre', 'precio-asc', 'precio-desc', 'recientes'].includes(
        params.get('orden') || '',
      )
        ? params.get('orden')!
        : 'nombre';
      this.porPagina = [12, 24, 48].includes(Number(params.get('cantidad')))
        ? Number(params.get('cantidad'))
        : 12;
      this.pagina = Math.max(1, Math.floor(Number(params.get('pagina')) || 1));
      this.filtrar();
    });
    this.cargar();
  }
  cargar(): void {
    this.cargando = true;
    this.error = '';
    this.service
      .cargar()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.data = data;
          this.cargando = false;
          this.filtrar();
        },
        error: () => {
          this.cargando = false;
          this.error =
            'No pudimos cargar el catálogo completo. Comprueba tu conexión e intenta nuevamente.';
        },
      });
  }
  get modelosDisponibles() {
    return this.data.modelos.filter(
      (m) =>
        m.activo &&
        (!this.filtros.marca || m.marcaId === this.filtros.marca) &&
        (!this.filtros.tipo || m.tipoVehiculo === this.filtros.tipo),
    );
  }
  get tipos() {
    return [
      ...new Set(this.data.modelos.map((m) => m.tipoVehiculo).filter((v): v is string => !!v)),
    ].sort();
  }
  get opcionesMarcas(): VehiclePickerOption[] {
    return this.data.marcas
      .filter((m) => m.activo)
      .map((m) => ({
        id: m.id,
        nombre: m.nombre,
        imagenUrl: m.imagenUrl,
        subtitulo: [
          m.paisOrigen,
          `${this.data.modelos.filter((modelo) => modelo.activo && modelo.marcaId === m.id).length} modelos`,
        ]
          .filter(Boolean)
          .join(' · '),
      }))
      .sort((a, b) => a.nombre.localeCompare(b.nombre));
  }
  get opcionesModelos(): VehiclePickerOption[] {
    return this.modelosDisponibles
      .map((m) => ({
        id: m.id,
        nombre: m.nombre,
        imagenUrl: m.imagenUrl,
        subtitulo: [
          this.data.marcas.find((marca) => marca.id === m.marcaId)?.nombre,
          m.tipoVehiculo,
        ]
          .filter(Boolean)
          .join(' · '),
      }))
      .sort((a, b) => a.nombre.localeCompare(b.nombre));
  }
  get motores() {
    return [
      ...new Set(
        this.data.compatibilidades
          .filter((c) => !this.filtros.modelo || c.modeloVehiculoId === this.filtros.modelo)
          .map((c) => c.motor)
          .filter((v): v is string => !!v),
      ),
    ].sort();
  }
  get monedas() {
    return [...new Set(this.data.productos.map((p) => p.moneda || 'PEN'))].sort();
  }
  get totalPaginas() {
    return Math.max(1, Math.ceil(this.resultados.length / this.porPagina));
  }
  get visibles() {
    return this.resultados.slice((this.pagina - 1) * this.porPagina, this.pagina * this.porPagina);
  }
  get desde() {
    return this.resultados.length ? (this.pagina - 1) * this.porPagina + 1 : 0;
  }
  get hasta() {
    return Math.min(this.pagina * this.porPagina, this.resultados.length);
  }
  get paginas() {
    const inicio = Math.max(1, Math.min(this.pagina - 2, this.totalPaginas - 4));
    return Array.from({ length: Math.min(5, this.totalPaginas) }, (_, i) => inicio + i);
  }
  get cantidadFiltros() {
    const { marca, modelo, ...resto } = this.filtros;
    return (
      Object.values(resto).filter((v) => v !== '' && v !== false).length + (marca || modelo ? 1 : 0)
    );
  }
  get rangoInvalido() {
    return (
      this.filtros.minimo !== '' &&
      this.filtros.maximo !== '' &&
      Number(this.filtros.minimo) > Number(this.filtros.maximo)
    );
  }
  precio(p: Producto) {
    return p.precioPromocional != null && p.precioPromocional < p.precio
      ? p.precioPromocional
      : p.precio;
  }
  stock(p: Producto): number | null {
    return this.data.stocks.find((s) => s.productoId === p.id)?.cantidad ?? null;
  }
  categoria(p: Producto): string {
    const ids = this.data.relaciones.filter((r) => r.productoId === p.id).map((r) => r.categoriaId);
    return this.data.categorias
      .filter((c) => ids.includes(c.id))
      .map((c) => c.nombre)
      .join(' · ');
  }
  compatibilidad(p: Producto): string {
    const ids = this.data.compatibilidades
      .filter((c) => c.productoId === p.id)
      .map((c) => c.modeloVehiculoId);
    return this.data.modelos
      .filter((m) => ids.includes(m.id))
      .map((m) =>
        `${this.data.marcas.find((b) => b.id === m.marcaId)?.nombre || ''} ${m.nombre}`.trim(),
      )
      .slice(0, 2)
      .join(' / ');
  }
  filtrar(): void {
    const f = this.filtros;
    const normal = (v: string) =>
      v
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLocaleLowerCase()
        .trim();
    const tieneVehiculo = !!(f.marca || f.modelo || f.anio || f.motor || f.tipo);
    const compatibles = new Set(
      this.data.compatibilidades
        .filter((c) => {
          const m = this.data.modelos.find((m) => m.id === c.modeloVehiculoId);
          return (
            (!f.marca || m?.marcaId === f.marca) &&
            (!f.modelo || c.modeloVehiculoId === f.modelo) &&
            (!f.tipo || m?.tipoVehiculo === f.tipo) &&
            (!f.motor || c.motor === f.motor) &&
            (!f.anio ||
              ((c.anioInicio == null || Number(f.anio) >= c.anioInicio) &&
                (c.anioFin == null || Number(f.anio) <= c.anioFin)))
          );
        })
        .map((c) => c.productoId),
    );
    const categoriaIds = new Set(
      this.data.relaciones.filter((r) => r.categoriaId === f.categoria).map((r) => r.productoId),
    );
    this.resultados = this.data.productos.filter(
      (p) =>
        p.activo &&
        (!f.q || normal(`${p.nombre} ${p.descripcion || ''} ${p.sku}`).includes(normal(f.q))) &&
        (!f.sku || normal(p.sku) === normal(f.sku)) &&
        (!f.categoria || categoriaIds.has(p.id)) &&
        (!f.proveedor || p.proveedorId === f.proveedor) &&
        (!tieneVehiculo || compatibles.has(p.id)) &&
        (!f.moneda || (p.moneda || 'PEN') === f.moneda) &&
        (f.minimo === '' || this.precio(p) >= Number(f.minimo)) &&
        (f.maximo === '' || this.precio(p) <= Number(f.maximo)) &&
        (!f.stock || (this.stock(p) ?? 0) > 0) &&
        (!f.oferta || this.precio(p) < p.precio),
    );
    this.resultados.sort((a, b) =>
      this.orden === 'precio-asc'
        ? this.precio(a) - this.precio(b)
        : this.orden === 'precio-desc'
          ? this.precio(b) - this.precio(a)
          : this.orden === 'recientes'
            ? (b.fechaCreacion || '').localeCompare(a.fechaCreacion || '')
            : a.nombre.localeCompare(b.nombre),
    );
    if (!this.cargando) this.pagina = Math.min(this.pagina, this.totalPaginas);
  }
  actualizarFiltros(esperar = false): void {
    clearTimeout(this.filtroPendiente);
                                                                    
    for (const campo of ['minimo', 'maximo', 'anio'] as const) {
      if (this.filtros[campo] == null) this.filtros[campo] = '';
    }
    if (esperar) this.filtroPendiente = setTimeout(() => this.aplicar(), 350);
    else this.aplicar();
  }
  cambiarMarca(): void {
    this.filtros.modelo = '';
    this.filtros.motor = '';
    this.actualizarFiltros();
  }
  seleccionarMarca(id: string): void {
    this.filtros.marca = id;
    this.cambiarMarca();
  }
  seleccionarModelo(id: string): void {
    this.filtros.modelo = id;
    const modelo = this.data.modelos.find((m) => m.id === id);
    if (modelo?.marcaId) this.filtros.marca = modelo.marcaId;
    this.cambiarModelo();
  }
  cambiarTipo(): void {
    this.filtros.modelo = '';
    this.filtros.motor = '';
    this.actualizarFiltros();
  }
  cambiarModelo(): void {
    this.filtros.motor = '';
    this.actualizarFiltros();
  }
  aplicar(): void {
    if (!this.rangoInvalido) {
      this.pagina = 1;
      this.filtrar();
      this.navegar(1);
    }
  }
  limpiar(): void {
    this.filtros = vacios();
    this.actualizarFiltros();
  }
  navegar(pagina: number): void {
    clearTimeout(this.filtroPendiente);
    const params: Record<string, string | number | boolean | null> = {};
    for (const [key, value] of Object.entries(this.filtros))
      params[key] = value === '' || value === false ? null : value;
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { ...params, orden: this.orden, cantidad: this.porPagina, pagina },
    });
  }
}
