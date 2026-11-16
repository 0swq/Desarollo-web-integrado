import { ParametrosService } from '../../../_services/parametros.service';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { PerfilService, PedidoItem } from '../../../_services/perfil.service';
import { PerfilModalService } from '../../../_services/perfil-modal.service';
import { Orden } from '../../../models/orden.interface';

@Component({ selector: 'app-mis-pedidos-page', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './mis-pedidos.html', styleUrl: './mis-pedidos.css' })
export class MisPedidosPage implements OnInit {
  private parametros=inject(ParametrosService);
  private service = inject(PerfilService); private destroyRef = inject(DestroyRef);
  readonly modal = inject(PerfilModalService);
  pedidos: Orden[] = []; cargando = true; error = ''; busqueda = ''; estado = ''; pagina = 1; readonly porPagina = 5;
  seleccionado: Orden | null = null; items: PedidoItem[] = []; cargandoDetalle = false; errorDetalle = '';
  readonly estados = [{ valor: 'PENDIENTE', nombre: 'Pendiente' }, { valor: 'PAGADO', nombre: 'Pagado' }, { valor: 'EN_PREPARACION', nombre: 'En preparación' }, { valor: 'ENVIADO', nombre: 'Enviado' }, { valor: 'ENTREGADO', nombre: 'Entregado' }, { valor: 'CANCELADO', nombre: 'Cancelado' }];
  get filtrados(): Orden[] { const q = this.busqueda.trim().toLowerCase(); return this.pedidos.filter(p => (!this.estado || p.estado === this.estado) && (!q || p.numeroOrden.toLowerCase().includes(q))); }
  get totalPaginas(): number { return Math.max(1, Math.ceil(this.filtrados.length / this.porPagina)); }
  get visibles(): Orden[] { return this.filtrados.slice((this.pagina - 1) * this.porPagina, this.pagina * this.porPagina); }
  nombreEstado(estado: string): string { return this.estados.find(e => e.valor === estado)?.nombre || estado; }
  ayuda(p: Orden): string|null { return this.parametros.whatsapp('Hola, quisiera consultar sobre mi pedido ' + p.numeroOrden + '.'); }
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.cargando = true; this.error = ''; this.seleccionado = null;
    this.service.pedidos().pipe(finalize(() => this.cargando = false), takeUntilDestroyed(this.destroyRef)).subscribe({
      next: pedidos => { this.pedidos = [...pedidos].sort((a,b) => (b.fechaCreacion || '').localeCompare(a.fechaCreacion || '')); this.pagina = 1; },
      error: () => this.error = 'No pudimos cargar tus pedidos. Vuelve a intentarlo.',
    });
  }
  filtrar(): void { this.pagina = 1; }
  verDetalle(pedido: Orden): void {
    this.seleccionado = pedido; this.items = []; this.cargandoDetalle = true; this.errorDetalle = '';
    this.service.items(pedido.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: items => { if (this.seleccionado?.id === pedido.id) { this.items = items; this.cargandoDetalle = false; } },
      error: () => { if (this.seleccionado?.id === pedido.id) { this.errorDetalle = 'No pudimos cargar los productos de este pedido.'; this.cargandoDetalle = false; } },
    });
  }
}
