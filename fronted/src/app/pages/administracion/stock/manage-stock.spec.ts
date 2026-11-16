import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../../environments/environment';
import { Stock } from '../../../models/stock.interface';
import { ManageStock } from './manage-stock';

describe('ManageStock', () => {
  let fixture: ComponentFixture<ManageStock>;
  let http: HttpTestingController;
  let panel: HTMLElement;
  let dialogo: HTMLDialogElement;
  const url = environment.apiUrl + '/stocks';
  const productos = [
    { id: 'p1', nombre: 'Lámpara halógena', sku: 'L-001', precio: 20, activo: true },
    { id: 'p2', nombre: 'Filtro de aceite', sku: 'F-002', precio: 40, activo: true },
    { id: 'p3', nombre: 'Refrigerante', sku: 'R-003', precio: 30, activo: true },
  ];
  const disponible: Stock = { id: 's1', productoId: 'p1', cantidad: 8, stockMinimo: 5, ubicacionAlmacen: 'Estante A' };
  const agotado: Stock = { id: 's2', productoId: 'p2', cantidad: 0, stockMinimo: 2 };
  const sinMinimo: Stock = { id: 's3', productoId: 'p3', cantidad: 4, stockMinimo: null };
  const porProducto = url + '/producto/' + disponible.productoId;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ManageStock],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    fixture = TestBed.createComponent(ManageStock);
    http = TestBed.inject(HttpTestingController);
    panel = fixture.nativeElement;
    fixture.detectChanges();
    dialogo = panel.querySelector('dialog')!;
    dialogo.showModal = () => { dialogo.open = true; };
    dialogo.close = () => { dialogo.open = false; };
  });

  afterEach(() => http.verify());

  it('cruza existencias con el catálogo y permite buscar y filtrar', async () => {
    cargar([disponible, agotado, sinMinimo], [agotado]);
    expect(panel.querySelectorAll('tbody tr')).toHaveLength(3);
    expect(panel.querySelector('tbody')?.textContent).toContain('Lámpara halógena');
    expect(panel.querySelector('tbody')?.textContent).toContain('L-001');
    expect(panel.querySelector('tbody')?.textContent).toContain('Sin mínimo');

    const filtro = panel.querySelector<HTMLSelectElement>('.toolbar select')!;
    filtro.value = 'bajo';
    filtro.dispatchEvent(new Event('change'));
    await fixture.whenStable();
    fixture.detectChanges();
    expect(panel.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(panel.querySelector('tbody .status')?.textContent).toContain('Agotado');

    filtro.value = 'todos';
    filtro.dispatchEvent(new Event('change'));
    escribir(panel.querySelector('input[type="search"]')!, 'lampara');
    await fixture.whenStable();
    fixture.detectChanges();
    expect(panel.querySelectorAll('tbody tr')).toHaveLength(1);
    expect(panel.querySelector('tbody tr')?.textContent).toContain('L-001');
  });

  it('calcula las alertas desde el listado si falla el endpoint de bajo stock', () => {
    cargar([{ ...disponible, cantidad: 5 }, agotado, sinMinimo], null);
    expect(panel.querySelector('.stats article:nth-child(2) strong')?.textContent).toBe('2');
    expect(panel.querySelectorAll('tbody tr')).toHaveLength(3);
  });

  it('consulta el detalle y guarda la configuración sin cambiar la cantidad', () => {
    cargar([disponible], []);
    abrir('Ver detalle', url + '/' + disponible.id);
    expect(dialogo.querySelector('.stock-details')?.textContent).toContain('Estante A');
    dialogo.querySelector<HTMLButtonElement>('.dialog-actions .refresh')!.click();

    abrir('Editar configuración', porProducto);
    expect(campo('stockMinimo').value).toBe('5');
    expect(campo('ubicacionAlmacen').value).toBe('Estante A');
    escribir(campo('stockMinimo'), '8');
    escribir(campo('ubicacionAlmacen'), ' Estante B ');
    enviar();

    const solicitud = http.expectOne({ method: 'PUT', url: porProducto });
    expect(solicitud.request.body).toEqual({ stockMinimo: 8, ubicacionAlmacen: 'Estante B' });
    solicitud.flush({ success: true, data: { ...disponible, stockMinimo: 8, ubicacionAlmacen: 'Estante B' } });
    fixture.detectChanges();
    expect(dialogo.open).toBe(false);
    expect(panel.querySelector('tbody tr')?.textContent).toContain('Estante B');
    expect(panel.querySelector('.stats article:nth-child(2) strong')?.textContent).toBe('1');
    expect(panel.querySelector('tbody td:nth-child(2)')?.textContent).toBe('8');
  });

  it('rechaza un mínimo no entero y permite quitar la ubicación con un guardado parcial', () => {
    cargar([disponible], []);
    abrir('Editar configuración', porProducto);
    escribir(campo('stockMinimo'), '1.5');
    enviar();
    http.expectNone({ method: 'PUT', url: porProducto });
    expect(dialogo.querySelector('[role="alert"]')?.textContent).toContain('stock mínimo entero');

    escribir(campo('stockMinimo'), '');
    escribir(campo('ubicacionAlmacen'), '');
    enviar();
    const solicitud = http.expectOne({ method: 'PUT', url: porProducto });
    expect(solicitud.request.body).toEqual({ ubicacionAlmacen: '' });
    solicitud.flush({ success: true, data: { ...disponible, ubicacionAlmacen: '' } });
    fixture.detectChanges();
    expect(panel.querySelector('tbody tr')?.textContent).toContain('Sin ubicación');
  });

  it('permite reintentar si falla el listado y distingue una base vacía', () => {
    http.expectOne(environment.apiUrl + '/productos').flush({ success: true, data: productos });
    http.expectOne(url + '/bajo-stock').flush({ success: true, data: [] });
    http.expectOne(url).flush({ message: 'Servicio no disponible' }, { status: 503, statusText: 'Unavailable' });
    fixture.detectChanges();
    expect(panel.querySelector('[role="alert"]')?.textContent).toContain('Servicio no disponible');
    panel.querySelector<HTMLButtonElement>('[role="alert"] button')!.click();
    cargar([], []);
    expect(panel.textContent).toContain('Aún no hay registros de stock');
  });

  function cargar(stocks: Stock[], bajos: Stock[] | null): void {
    http.expectOne({ method: 'GET', url }).flush({ success: true, data: stocks });
    http.expectOne(environment.apiUrl + '/productos').flush({ success: true, data: productos });
    const alertas = http.expectOne(url + '/bajo-stock');
    if (bajos == null) alertas.flush({}, { status: 500, statusText: 'Internal Server Error' });
    else alertas.flush({ success: true, data: bajos });
    fixture.detectChanges();
  }

  function abrir(accion: string, ruta: string): void {
    panel.querySelector<HTMLButtonElement>('button[title="' + accion + '"]')!.click();
    http.expectOne({ method: 'GET', url: ruta }).flush({ success: true, data: disponible });
    fixture.detectChanges();
  }

  function campo(nombre: string): HTMLInputElement {
    return dialogo.querySelector('[formcontrolname="' + nombre + '"]')!;
  }

  function enviar(): void {
    dialogo.querySelector('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
  }
});

function escribir(input: HTMLInputElement, valor: string): void {
  input.value = valor;
  input.dispatchEvent(new Event('input'));
}
