import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../models/auth.interface';
import { CurrencyPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { toast } from 'ngx-sonner';
import { finalize, forkJoin, of, switchMap, Subscription } from 'rxjs';
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

  protected readonly archivoImagen = signal<File | null>(null);
  protected readonly vistaPrevia = signal('');
  protected readonly errorImagen = signal('');
  protected readonly arrastrando = signal(false);
  private limpiarImagen(): void { if(this.vistaPrevia().startsWith('blob:')) URL.revokeObjectURL(this.vistaPrevia()); this.archivoImagen.set(null);this.vistaPrevia.set('');this.errorImagen.set('');this.arrastrando.set(false); }
  protected seleccionarArchivo(event: Event): void {const input=event.target as HTMLInputElement;this.recibirImagen(input.files);input.value='';}
  protected soltarImagen(event: DragEvent): void {event.preventDefault();this.arrastrando.set(false);if(!this.procesando())this.recibirImagen(event.dataTransfer?.files);}
  private recibirImagen(files?: FileList|null): void {if(this.procesando()||!files?.length)return;if(files.length!==1){this.errorImagen.set('Selecciona una sola imagen.');return;}const file=files[0];if(!['image/jpeg','image/png','image/webp'].includes(file.type)){this.errorImagen.set('Usa una imagen JPG, PNG o WebP.');return;}if(!file.size||file.size>10*1024*1024){this.errorImagen.set('La imagen debe tener contenido y pesar como máximo 10 MB.');return;}const url=URL.createObjectURL(file);const img=new Image();img.onload=()=>{if(this.vistaPrevia().startsWith('blob:'))URL.revokeObjectURL(this.vistaPrevia());this.archivoImagen.set(file);this.vistaPrevia.set(url);this.errorImagen.set('');};img.onerror=()=>{URL.revokeObjectURL(url);this.errorImagen.set('El archivo no contiene una imagen válida.');};img.src=url;}
  protected quitarImagen(): void {if(this.procesando())return;this.limpiarImagen();this.formulario.controls.imagenUrl.setValue('');}
  private peticionPagina?: Subscription; private temporizador?: ReturnType<typeof setTimeout>;
  private readonly http=inject(HttpClient);
  protected readonly pagina=signal(1);
  protected readonly total=signal(0);
  protected readonly totalPaginas=signal(1);
  protected buscar(texto:string){this.busqueda.set(texto);this.pagina.set(1);clearTimeout(this.temporizador);this.temporizador=setTimeout(()=>this.cargar(),250);}
  protected irPagina(p:number){if(p<1||p>this.totalPaginas()||this.cargando())return;this.pagina.set(p);this.cargar();}
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
  protected readonly visibles = computed(()=>this.productos());

  ngOnInit(): void {
    this.destroyRef.onDestroy(()=>this.limpiarImagen());
    this.cargar();
  }

  protected cargar(): void {
    this.peticionPagina?.unsubscribe();

    this.cargando.set(true);
    this.error.set('');
    this.peticionPagina=this.http.get<ApiResponse<{contenido:Producto[];total:number;paginas:number}>>(`${environment.apiUrl}/productos/pagina`,{params:{pagina:this.pagina()-1,tamano:10,q:this.busqueda()}}).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.cargando.set(false)),
    ).subscribe({
      next: productos => {
        this.fotosFallidas.clear();
        if(!productos.success||!productos.data){this.error.set('No se pudo cargar la página.');return;}
        this.productos.set(productos.data.contenido);this.total.set(productos.data.total);this.totalPaginas.set(Math.max(1,productos.data.paginas));
      },
      error: error => this.error.set(mensajeError(error, 'No se pudo consultar el catálogo.')),
    });
  }

  protected abrirEditor(producto?: Producto): void {
    this.limpiarImagen();this.editandoId.set(producto?.id ?? null);
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
        this.vistaPrevia.set(this.catalogo.imagenPublica(producto?.imagenUrl));
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
    if (!id && (stockInicial == null || !Number.isInteger(stockInicial) || stockInicial < 1 || stockInicial > 2147483647)) {
      this.errorAccion.set('El stock inicial debe ser un número entero mayor que cero.');
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
    (this.archivoImagen() ? this.catalogo.subirImagen(this.archivoImagen()!, 'productos') : of<{ruta:string;url:string}|null>(null)).pipe(
      switchMap(imagen => {if(imagen) {solicitud.imagenUrl=imagen.url;this.formulario.controls.imagenUrl.setValue(imagen.url);this.archivoImagen.set(null);}return this.catalogo.guardar<Producto>('productos', solicitud, id ?? undefined);}),
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.procesando.set(false)),
    ).subscribe({
      next: producto => {
        this.fotosFallidas.delete(producto.id);
        this.productos.update(productos => id
          ? productos.map(item => item.id === id ? producto : item)
          : [producto, ...productos]);
        this.cargar();
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
