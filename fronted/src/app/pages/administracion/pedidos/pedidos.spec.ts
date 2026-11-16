import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../../environments/environment';
import { Orden } from '../../../models/orden.interface';
import { PedidosAdminPage } from './pedidos';

describe('PedidosAdminPage', () => {
  let fixture: ComponentFixture<PedidosAdminPage>;
  let http: HttpTestingController;
  let panel: HTMLElement;
  let dialogo: HTMLDialogElement;
  const url = `${environment.apiUrl}/ordenes`;
  const pedido: Orden = {
    id: 'p1', numeroOrden: 'ORD-001', estado: 'PENDIENTE', fechaCreacion: '2026-11-14T10:00:00',
    direccionEntrega: 'Dirección de prueba', telefonoContacto: '5550123', subtotal: 100, igv: 18, total: 118,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PedidosAdminPage],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(PedidosAdminPage);
    http = TestBed.inject(HttpTestingController);
    panel = fixture.nativeElement;
    fixture.detectChanges();
    dialogo = panel.querySelector('dialog')!;
    dialogo.showModal = () => { dialogo.open = true; };
    dialogo.close = () => { dialogo.open = false; };
  });

  afterEach(() => http.verify());

  it('carga los pedidos, filtra por estado y enlaza el alta con el carrito', async () => {
    listar([pedido, { ...pedido, id: 'p2', numeroOrden: 'ORD-002', estado: 'ENTREGADO', fechaCreacion: '2026-11-15T10:00:00' }]);
    expect(panel.querySelector('tbody tr')?.textContent).toContain('ORD-002');
    expect(panel.querySelector('.heading-actions a')?.getAttribute('href')).toBe('/carrito');
    expect(panel.querySelector('.stats article:nth-child(2) strong')?.textContent).toBe('1');

    elegir(panel.querySelector('.toolbar select')!, 'PENDIENTE');
    await fixture.whenStable();
    fixture.detectChanges();
    expect(panel.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(panel.querySelector('tbody tr')?.textContent).toContain(pedido.numeroOrden);
  });

  it('consulta el detalle y guarda un cambio de estado en la API', () => {
    listar([pedido]);
    abrir('Ver detalle');
    expect(dialogo.querySelector('.order-details')?.textContent).toContain(pedido.direccionEntrega);
    expect(dialogo.querySelector('.order-totals')?.textContent).toContain('118.00');
    dialogo.querySelector<HTMLButtonElement>('.dialog-actions .refresh')!.click();

    abrir('Editar estado');
    const select = dialogo.querySelector('select')!;
    elegir(select, 'EN_PREPARACION');
    fixture.detectChanges();
    dialogo.querySelector<HTMLButtonElement>('.dialog-actions .primary')!.click();
    const solicitud = http.expectOne({ method: 'PATCH', url: `${url}/${pedido.id}/estado` });
    expect(solicitud.request.body).toEqual({ estado: 'EN_PREPARACION' });
    solicitud.flush({ success: true, data: { ...pedido, estado: 'EN_PREPARACION' } });
    fixture.detectChanges();
    expect(panel.querySelector('tbody .status')?.textContent).toContain('En preparación');
  });

  it('confirma una sola cancelación y conserva el pedido en el historial', () => {
    listar([pedido]);
    abrir('Cancelar pedido');
    expect(dialogo.textContent).toContain('volverán al stock');
    http.expectNone(r => r.method === 'PATCH' || r.method === 'DELETE');

    const confirmar = dialogo.querySelector<HTMLButtonElement>('.dialog-actions .primary')!;
    confirmar.click();
    confirmar.click();
    const solicitud = http.expectOne({ method: 'PATCH', url: `${url}/${pedido.id}/estado` });
    expect(solicitud.request.body).toEqual({ estado: 'CANCELADO' });
    solicitud.flush({ success: true, data: { ...pedido, estado: 'CANCELADO' } });
    fixture.detectChanges();
    expect(dialogo.open).toBe(false);
    expect(panel.querySelector('tbody .status')?.textContent).toContain('Cancelado');
    expect(panel.querySelector<HTMLButtonElement>('button[title="Editar estado"]')?.disabled).toBe(true);
  });

  function listar(pedidos: Orden[]): void {
    http.expectOne({ method: 'GET', url }).flush({ success: true, data: pedidos });
    fixture.detectChanges();
  }

  function abrir(accion: string): void {
    panel.querySelector<HTMLButtonElement>(`button[title="${accion}"]`)!.click();
    http.expectOne({ method: 'GET', url: `${url}/${pedido.id}` }).flush({ success: true, data: pedido });
    fixture.detectChanges();
  }
});

function elegir(select: HTMLSelectElement, estado: string): void {
  select.value = estado;
  select.dispatchEvent(new Event('change'));
}
