import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { toast } from 'ngx-sonner';
import { finalize, Subscription } from 'rxjs';
import { OrdenService } from '../../../_services/orden';
import { EstadoOrden } from '../../../models/estado-orden';
import { Orden } from '../../../models/orden.interface';

type AccionPedido = 'detalle' | 'editar' | 'cancelar';

const NOMBRES_ESTADO: Record<EstadoOrden, string> = {
  PENDIENTE: 'Pendiente',
  PAGADO: 'Pagado',
  EN_PREPARACION: 'En preparación',
  ENVIADO: 'Enviado',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
};

@Component({
  selector: 'app-pedidos-page',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink],
  templateUrl: './pedidos.html',
  styleUrl: './pedidos.css',
})
export class PedidosAdminPage implements OnInit {
  private readonly ordenes = inject(OrdenService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly dialogo = viewChild.required<ElementRef<HTMLDialogElement>>('dialogo');
  private solicitudDetalle?: Subscription;

  protected readonly nombresEstado = NOMBRES_ESTADO;
  protected readonly estados = Object.keys(NOMBRES_ESTADO) as EstadoOrden[];
  protected readonly estadosEditables: readonly EstadoOrden[] = this.estados.filter(estado => estado !== 'CANCELADO');
  protected readonly pedidos = signal<Orden[]>([]);
  protected readonly busqueda = signal('');
  protected readonly filtroEstado = signal<EstadoOrden | ''>('');
  protected readonly cargando = signal(false);
  protected readonly error = signal('');

  protected readonly accion = signal<AccionPedido>('detalle');
  private readonly pedidoId = signal('');
  protected readonly detalle = signal<Orden | null>(null);
  protected readonly estadoNuevo = signal<EstadoOrden | ''>('');
  protected readonly errorDetalle = signal('');
  protected readonly errorAccion = signal('');
  protected readonly procesando = signal(false);

  protected readonly pendientes = computed(() => this.pedidos().filter(pedido => pedido.estado === 'PENDIENTE').length);
  protected readonly visibles = computed(() => {
    const estado = this.filtroEstado();
    const termino = this.busqueda().trim().toLocaleLowerCase();
    return this.pedidos().filter(pedido =>
      (!estado || pedido.estado === estado)
      && (!termino || [pedido.numeroOrden, pedido.direccionEntrega, pedido.telefonoContacto]
        .join(' ').toLocaleLowerCase().includes(termino)),
    );
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    if (this.cargando() || this.procesando()) return;

    this.cargando.set(true);
    this.error.set('');
    this.ordenes.listar().pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.cargando.set(false)),
    ).subscribe({
      next: pedidos => this.pedidos.set([...pedidos].sort((a, b) =>
        (b.fechaCreacion ?? '').localeCompare(a.fechaCreacion ?? ''),
      )),
      error: error => this.error.set(mensajeError(error, 'No se pudo consultar el listado de pedidos.')),
    });
  }

  protected abrir(accion: AccionPedido, pedido: Orden): void {
    this.accion.set(accion);
    this.pedidoId.set(pedido.id);
    this.cargarDetalle();
    this.dialogo().nativeElement.showModal();
  }

  protected cargarDetalle(): void {
    this.solicitudDetalle?.unsubscribe();
    this.detalle.set(null);
    this.errorDetalle.set('');
    this.errorAccion.set('');
    this.solicitudDetalle = this.ordenes.obtener(this.pedidoId()).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: pedido => {
        this.detalle.set(pedido);
        this.estadoNuevo.set(pedido.estado);
        this.actualizarFila(pedido);
      },
      error: error => this.errorDetalle.set(mensajeError(error, 'No se pudo consultar el pedido.')),
    });
  }

  protected cerrar(evento?: Event): void {
    evento?.preventDefault();
    if (this.procesando()) return;

    this.solicitudDetalle?.unsubscribe();
    this.dialogo().nativeElement.close();
  }

  protected guardar(): void {
    const pedido = this.detalle();
    const accion = this.accion();
    if (this.procesando() || !pedido || pedido.estado === 'CANCELADO' || accion === 'detalle') return;

    const estado = accion === 'cancelar' ? 'CANCELADO' : this.estadoNuevo();
    if (!estado || estado === pedido.estado || (accion === 'editar' && !this.estadosEditables.includes(estado))) return;

    this.procesando.set(true);
    this.errorAccion.set('');
    this.ordenes.actualizarEstado(pedido.id, estado).pipe(
      takeUntilDestroyed(this.destroyRef),
      finalize(() => this.procesando.set(false)),
    ).subscribe({
      next: actualizado => {
        this.actualizarFila(actualizado);
        this.dialogo().nativeElement.close();
        toast.success(accion === 'cancelar' ? 'Pedido cancelado.' : 'Estado del pedido actualizado.');
      },
      error: error => this.errorAccion.set(mensajeError(error, 'No se pudo guardar el estado del pedido.')),
    });
  }

  private actualizarFila(pedido: Orden): void {
    this.pedidos.update(pedidos => pedidos.map(item => item.id === pedido.id ? pedido : item));
  }
}

function mensajeError(error: unknown, alternativa: string): string {
  if (error instanceof HttpErrorResponse) {
    if (typeof error.error?.message === 'string') return error.error.message;
    if (error.status === 403) return 'Tu cuenta no tiene permiso para realizar esta acción.';
    return alternativa;
  }
  return error instanceof Error ? error.message : alternativa;
}
