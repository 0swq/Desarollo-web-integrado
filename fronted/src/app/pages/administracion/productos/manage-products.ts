import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { toast } from 'ngx-sonner';
import { finalize, forkJoin, of, Subscription } from 'rxjs';
import { AdminCatalogoService } from '../../../_services/admin-catalogo.service';
import { ProductoService } from '../../../_services/producto.service';
import { Categoria } from '../../../models/categoria.interface';
import { Producto } from '../../../models/producto.interface';
import { Proveedor } from '../../../models/proveedor.interface';

interface DatosEditor {
  categorias: Categoria[];
  proveedores: Proveedor[];
  relaciones: { categoriaId: string }[];
  producto: Producto | null;
}

@Component({
  selector: 'app-manage-products',
  imports: [CurrencyPipe, FormsModule, ReactiveFormsModule],
  templateUrl: './manage-products.html',
  styleUrl: './manage-products.css',
})
export class ManageProducts implements OnInit {
  protected readonly catalogo = inject(AdminCatalogoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly apiProductos = inject(ProductoService);
  private readonly editor = viewChild.required<ElementRef<HTMLDialogElement>>('editor');
  private readonly confirmacion = viewChild.required<ElementRef<HTMLDialogElement>>('confirmacion');
  private cargaEditor?: Subscription;

  protected readonly productos = signal<Producto[]>([]);
  protected readonly busqueda = signal('');
  protected readonly cargando = signal(false);
  protected readonly error = signal('');
  protected readonly fotosFallidas = new Set<string>();
  protected readonly editandoId = signal<string | null>(null);
  protected readonly productoParaEliminar = signal<Producto | null>(null);
  protected readonly datosEditor = signal<DatosEditor | null>(null);
  protected readonly errorCargaEditor = signal('');
  protected readonly errorAccion = signal('');
  protected readonly procesando = signal(false);

  protected readonly formulario = inject(FormBuilder).nonNullable.group({
    sku: '',
    nombre: '',
    descripcion: '',
    precio: new FormControl<number | null>(null),
    precioPromocional: new FormControl<number | null>(null),
    categoriaId: '',
    proveedorId: '',
    imagenUrl: '',
    stockInicial: new FormControl<number | null>(0),
    activo: true,
  });

  protected readonly activos = computed(() => this.productos().filter(producto => producto.activo).length);
  protected readonly visibles = computed(() => {
    const termino = normalizar(this.busqueda().trim());
    const productos = this.productos();

    return termino ? productos.filter(producto =>
      normalizar(`${producto.nombre} ${producto.sku} ${producto.descripcion ?? ''}`).includes(termino),
    ) : productos;
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    if (this.cargando()) return;

    this.cargando.set(true);
    this.error.set('');
    this.catalogo.listar<Producto>('productos').pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.cargando.set(false)),
    ).subscribe({
      next: productos => {
        this.fotosFallidas.clear();
        this.productos.set(productos);
      },
      error: error => this.error.set(mensajeError(error, 'No se pudo consultar el catálogo.')),
    });
  }

  protected abrirEditor(producto?: Producto): void {
    this.editandoId.set(producto?.id ?? null);
    this.editor().nativeElement.showModal();
    this.cargarEditor();
  }

  protected cargarEditor(): void {
    this.cargaEditor?.unsubscribe();
    this.datosEditor.set(null);
    this.errorCargaEditor.set('');
    this.errorAccion.set('');

    const id = this.editandoId();
    this.cargaEditor = forkJoin({
      categorias: this.catalogo.listar<Categoria>('categorias'),
      proveedores: this.catalogo.listar<Proveedor>('proveedores'),
      relaciones: id ? this.catalogo.categoriasDeProducto(id) : of([]),
      producto: id ? this.apiProductos.obtenerProducto(id) : of(null),
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: datos => {
        const { producto, relaciones } = datos;
        this.formulario.reset({
          sku: producto?.sku ?? '',
          nombre: producto?.nombre ?? '',
          descripcion: producto?.descripcion ?? '',
          precio: producto?.precio ?? null,
          precioPromocional: producto?.precioPromocional ?? null,
          categoriaId: relaciones.length === 1 ? relaciones[0].categoriaId : '',
          proveedorId: producto?.proveedorId ?? producto?.proveedor?.id ?? '',
          imagenUrl: producto?.imagenUrl ?? '',
          stockInicial: 0,
          activo: producto?.activo ?? true,
        });
        this.datosEditor.set(datos);
      },
      error: error => this.errorCargaEditor.set(mensajeError(error, 'No pudimos cargar los datos del formulario.')),
    });
  }

  protected cerrarEditor(evento?: Event): void {
    evento?.preventDefault();
    if (this.procesando()) return;

    this.cargaEditor?.unsubscribe();
    this.editor().nativeElement.close();
  }

  protected guardar(): void {
    const datosEditor = this.datosEditor();
    if (this.procesando() || !datosEditor) return;

    this.formulario.markAllAsTouched();
    const datos = this.formulario.getRawValue();
    const sku = datos.sku.trim();
    const nombre = datos.nombre.trim();
    const { precio, precioPromocional, stockInicial } = datos;
    const id = this.editandoId();
    if (this.formulario.invalid || !sku || !nombre || precio == null) {
      this.errorAccion.set('Revisa el SKU, el nombre, la categoría y los valores del formulario.');
      return;
    }
    const cambioPrecio = datosEditor.producto?.precio !== precio
      || datosEditor.producto?.precioPromocional !== precioPromocional;
    if (cambioPrecio && precioPromocional != null && (precioPromocional <= 0 || precioPromocional >= precio)) {
      this.errorAccion.set('El precio promocional debe ser mayor que cero y menor que el precio regular.');
      return;
    }
    if (!id && stockInicial != null && !Number.isInteger(stockInicial)) {
      this.errorAccion.set('El stock inicial debe ser un número entero.');
      return;
    }

    const solicitud = {
      sku,
      nombre,
      descripcion: datos.descripcion.trim() || null,
      precio,
      precioPromocional,
      categoriaId: datos.categoriaId,
      proveedorId: datos.proveedorId || null,
      imagenUrl: datos.imagenUrl.trim() || null,
      activo: datos.activo,
      ...(!id && { stockInicial: stockInicial ?? 0 }),
    };

    this.procesando.set(true);
    this.errorAccion.set('');
    this.catalogo.guardar<Producto>('productos', solicitud, id ?? undefined).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.procesando.set(false)),
    ).subscribe({
      next: producto => {
        this.fotosFallidas.delete(producto.id);
        this.productos.update(productos => id
          ? productos.map(item => item.id === id ? producto : item)
          : [producto, ...productos]);
        this.editor().nativeElement.close();
        toast.success(id ? 'Producto actualizado.' : 'Producto creado.');
      },
      error: error => this.errorAccion.set(mensajeError(error, 'No se pudo guardar el producto.')),
    });
  }

  protected solicitarEliminar(producto: Producto): void {
    this.productoParaEliminar.set(producto);
    this.errorAccion.set('');
    this.confirmacion().nativeElement.showModal();
  }

  protected cerrarConfirmacion(evento?: Event): void {
    evento?.preventDefault();
    if (!this.procesando()) this.confirmacion().nativeElement.close();
  }

  protected eliminar(): void {
    const producto = this.productoParaEliminar();
    if (this.procesando() || !producto) return;

    this.procesando.set(true);
    this.errorAccion.set('');
    this.catalogo.eliminar('productos', producto.id).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.procesando.set(false)),
    ).subscribe({
      next: () => {
        this.productos.update(productos => productos.map(item =>
          item.id === producto.id ? { ...item, activo: false } : item,
        ));
        this.confirmacion().nativeElement.close();
        toast.success('Producto eliminado del catálogo de la tienda.');
      },
      error: error => this.errorAccion.set(mensajeError(error, 'No se pudo eliminar el producto.')),
    });
  }
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
