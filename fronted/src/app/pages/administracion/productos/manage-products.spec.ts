import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../../environments/environment';
import { ManageProducts } from './manage-products';

describe('ManageProducts', () => {
  let fixture: ComponentFixture<ManageProducts>;
  let http: HttpTestingController;
  let panel: HTMLElement;
  const url = `${environment.apiUrl}/productos`;
  const producto = { id: 'p1', sku: 'FR-001', nombre: 'Pastillas de freno', precio: 45, activo: true };
  const categoria = { id: 'c1', nombre: 'Frenos', activo: true };
  const proveedor = { id: 'pr1', razonSocial: 'Proveedor demo', activo: true };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ManageProducts],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    fixture = TestBed.createComponent(ManageProducts);
    http = TestBed.inject(HttpTestingController);
    panel = fixture.nativeElement;
    fixture.detectChanges();
    for (const dialogo of panel.querySelectorAll('dialog')) {
      dialogo.showModal = () => { dialogo.open = true; };
      dialogo.close = () => { dialogo.open = false; };
    }
  });

  afterEach(() => http.verify());

  it('lista productos activos e inactivos de la API y permite buscar por SKU o descripción', async () => {
    expect(panel.querySelector('[role="status"]')?.textContent).toContain('Cargando productos');
    http.expectOne({ method: 'GET', url }).flush({ success: true, data: [
      { id: '1', nombre: 'Pastillas de freno', sku: 'FR-001', precio: 45, activo: true },
      { id: '2', nombre: 'Filtro de aceite', sku: 'FL-002', descripcion: 'Filtración de impurezas', precio: 30, activo: false },
    ] });
    await fixture.whenStable();
    fixture.detectChanges();

    const filas = () => Array.from(panel.querySelectorAll('tbody tr'));
    expect(filas()).toHaveLength(2);
    expect(filas()[0].textContent).toContain('FR-001');
    expect(filas()[0].textContent).toContain('45.00');
    expect(filas()[1].querySelector('.status')?.textContent).toContain('Inactivo');
    expect(panel.querySelector('.stats article:nth-child(2) strong')?.textContent).toBe('1');

    const buscar = panel.querySelector<HTMLInputElement>('input[type="search"]')!;
    buscar.value = ' fr-001 ';
    buscar.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    fixture.detectChanges();
    expect(filas()).toHaveLength(1);
    expect(filas()[0].textContent).toContain('Pastillas de freno');

    buscar.value = 'filtracion';
    buscar.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    fixture.detectChanges();
    expect(filas()).toHaveLength(1);
    expect(filas()[0].textContent).toContain('Filtro de aceite');
  });

  it('distingue un error de una lista vacía y vuelve a consultar al reintentar', () => {
    http.expectOne(url).flush({ message: 'Servicio no disponible' }, { status: 503, statusText: 'Unavailable' });
    fixture.detectChanges();
    const alerta = panel.querySelector('[role="alert"]')!;
    expect(alerta.textContent).toContain('Servicio no disponible');

    alerta.querySelector('button')!.click();
    http.expectOne({ method: 'GET', url }).flush({ success: true, data: [] });
    fixture.detectChanges();
    expect(panel.querySelector('[role="alert"]')).toBeNull();
    expect(panel.textContent).toContain('Aún no hay productos');
  });

  it('crea un producto con su categoría y stock inicial y lo agrega al listado', () => {
    http.expectOne(url).flush({ success: true, data: [] });
    fixture.detectChanges();
    panel.querySelector<HTMLButtonElement>('.heading-actions .primary')!.click();
    cargarOpciones();
    fixture.detectChanges();

    const form = panel.querySelector<HTMLFormElement>('dialog[open] form')!;
    escribir(form, 'sku', ' fr-001 ');
    escribir(form, 'nombre', ' Pastillas de freno ');
    escribir(form, 'precio', '45');
    escribir(form, 'categoriaId', categoria.id);
    escribir(form, 'proveedorId', proveedor.id);
    escribir(form, 'stockInicial', '4');
    enviar(form);

    const solicitud = http.expectOne({ method: 'POST', url });
    expect(solicitud.request.body).toMatchObject({
      sku: 'fr-001', nombre: producto.nombre, precio: 45, categoriaId: categoria.id,
      proveedorId: proveedor.id, stockInicial: 4, activo: true,
    });
    solicitud.flush({ success: true, data: producto }, { status: 201, statusText: 'Created' });
    fixture.detectChanges();
    expect(panel.querySelector('dialog[open]')).toBeNull();
    expect(panel.querySelector('tbody tr')?.textContent).toContain(producto.nombre);
  });

  it('precarga los datos de la API y guarda los cambios sin enviar el stock de nuevo', () => {
    http.expectOne(url).flush({ success: true, data: [producto] });
    fixture.detectChanges();
    panel.querySelector<HTMLButtonElement>('button[title="Editar"]')!.click();
    cargarOpciones();
    http.expectOne(`${environment.apiUrl}/producto-categorias/producto/${producto.id}`)
      .flush({ success: true, data: [{ categoriaId: categoria.id }] });
    http.expectOne(`${url}/${producto.id}`).flush({ success: true, data: {
      ...producto, precio: 48, proveedorId: proveedor.id, imagenUrl: '/api/storage/imagenes/productos/freno.png',
    } });
    fixture.detectChanges();
    TestBed.tick();

    const form = panel.querySelector<HTMLFormElement>('dialog[open] form')!;
    expect(campo(form, 'precio').value).toBe('48');
    expect(campo(form, 'categoriaId').value).toBe(categoria.id);
    expect(campo(form, 'proveedorId').value).toBe(proveedor.id);
    escribir(form, 'nombre', 'Pastillas de freno delanteras');
    enviar(form);

    const solicitud = http.expectOne({ method: 'PUT', url: `${url}/${producto.id}` });
    expect(solicitud.request.body).toMatchObject({
      nombre: 'Pastillas de freno delanteras', precio: 48, categoriaId: categoria.id,
      proveedorId: proveedor.id, imagenUrl: '/api/storage/imagenes/productos/freno.png',
    });
    expect(solicitud.request.body).not.toHaveProperty('stockInicial');
    solicitud.flush({ success: true, data: { ...producto, ...solicitud.request.body } });
    fixture.detectChanges();
    expect(panel.querySelector('tbody tr')?.textContent).toContain('Pastillas de freno delanteras');
  });

  it('confirma la eliminación y refleja la baja lógica en el listado', () => {
    http.expectOne(url).flush({ success: true, data: [producto] });
    fixture.detectChanges();
    const eliminar = panel.querySelector<HTMLButtonElement>('button[title="Eliminar"]')!;
    eliminar.click();
    fixture.detectChanges();
    http.expectNone({ method: 'DELETE', url: `${url}/${producto.id}` });
    panel.querySelector<HTMLButtonElement>('dialog[open] .refresh')!.click();
    expect(panel.querySelector('dialog[open]')).toBeNull();
    http.expectNone({ method: 'DELETE', url: `${url}/${producto.id}` });

    eliminar.click();
    fixture.detectChanges();
    panel.querySelector<HTMLButtonElement>('dialog[open] .primary')!.click();
    http.expectOne({ method: 'DELETE', url: `${url}/${producto.id}` })
      .flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();
    expect(panel.querySelector('dialog[open]')).toBeNull();
    expect(panel.querySelector('tbody .status')?.textContent).toContain('Inactivo');
    expect(eliminar.disabled).toBe(true);
  });

  function cargarOpciones(): void {
    http.expectOne(`${environment.apiUrl}/categorias`).flush({ success: true, data: [categoria] });
    http.expectOne(`${environment.apiUrl}/proveedores`).flush({ success: true, data: [proveedor] });
  }
});

function campo(form: HTMLFormElement, nombre: string): HTMLInputElement | HTMLSelectElement {
  return form.querySelector(`[formcontrolname="${nombre}"]`)!;
}

function escribir(form: HTMLFormElement, nombre: string, valor: string): void {
  const control = campo(form, nombre);
  control.value = valor;
  control.dispatchEvent(new Event(control instanceof HTMLSelectElement ? 'change' : 'input'));
}

function enviar(form: HTMLFormElement): void {
  form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
}
