import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../../environments/environment';
import { Proveedor } from '../../../models/proveedor.interface';
import { ProveedoresPage } from './proveedores';

describe('ProveedoresPage', () => {
  let fixture: ComponentFixture<ProveedoresPage>;
  let http: HttpTestingController;
  let panel: HTMLElement;
  let dialogo: HTMLDialogElement;
  const url = `${environment.apiUrl}/proveedores`;
  const proveedor = {
    id: 'p1', ruc: '20999999999', razonSocial: 'Iluminación del norte', contactoNombre: 'Lucía',
    telefono: '5550123', correo: 'contacto@example.test', direccion: 'Av. Norte 10', activo: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ProveedoresPage],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    fixture = TestBed.createComponent(ProveedoresPage);
    http = TestBed.inject(HttpTestingController);
    panel = fixture.nativeElement;
    fixture.detectChanges();
    dialogo = panel.querySelector('dialog')!;
    dialogo.showModal = () => { dialogo.open = true; };
    dialogo.close = () => { dialogo.open = false; };
  });

  afterEach(() => http.verify());

  it('lista y permite buscar proveedores por razón social, RUC y contacto', async () => {
    listar([proveedor, { id: 'p2', ruc: '20999999998', razonSocial: 'Frenos Central', activo: false }]);
    expect(panel.querySelectorAll('tbody tr')).toHaveLength(2);
    expect(panel.querySelector('.stats article:nth-child(2) strong')?.textContent).toBe('1');

    const buscar = panel.querySelector<HTMLInputElement>('input[type="search"]')!;
    for (const termino of [' iluminacion ', proveedor.ruc, 'lucia']) {
      escribir(buscar, termino);
      await fixture.whenStable();
      fixture.detectChanges();
      expect(panel.querySelectorAll('tbody tr')).toHaveLength(1);
      expect(panel.querySelector('tbody tr')?.textContent).toContain(proveedor.razonSocial);
    }
  });

  it('valida el RUC y agrega el proveedor creado a la tabla', () => {
    listar([]);
    panel.querySelector<HTMLButtonElement>('.heading-actions .primary')!.click();
    escribir(campo('ruc'), '123');
    escribir(campo('razonSocial'), ' Iluminación del norte ');
    enviar();
    http.expectNone({ method: 'POST', url });
    expect(dialogo.querySelector('[role="alert"]')?.textContent).toContain('11 dígitos');

    escribir(campo('ruc'), proveedor.ruc);
    const datos = { ruc: proveedor.ruc, razonSocial: proveedor.razonSocial, activo: true };
    enviar();
    const solicitud = http.expectOne({ method: 'POST', url });
    expect(solicitud.request.body).toMatchObject(datos);
    solicitud.flush({ success: true, data: { id: proveedor.id, ...datos } });
    fixture.detectChanges();
    expect(dialogo.open).toBe(false);
    expect(panel.querySelector('tbody tr')?.textContent).toContain(proveedor.razonSocial);
  });

  it('precarga el contacto y conserva los demás datos al editar', () => {
    listar([proveedor]);
    panel.querySelector<HTMLButtonElement>('button[title="Editar"]')!.click();
    fixture.detectChanges();
    expect(campo('ruc').value).toBe(proveedor.ruc);
    expect(campo('correo').value).toBe(proveedor.correo);
    escribir(campo('telefono'), '5550124');
    enviar();

    const solicitud = http.expectOne({ method: 'PUT', url: `${url}/${proveedor.id}` });
    const { id, ...datos } = proveedor;
    expect(solicitud.request.body).toEqual({ ...datos, telefono: '5550124' });
    solicitud.flush({ success: true, data: { id, ...solicitud.request.body } });
    fixture.detectChanges();
    expect(panel.querySelector('tbody tr')?.textContent).toContain('5550124');
  });

  it('confirma la baja y permite reactivar el proveedor desde la tabla', () => {
    listar([proveedor]);
    const eliminar = panel.querySelector<HTMLButtonElement>('button[title="Eliminar"]')!;
    eliminar.click();
    fixture.detectChanges();
    dialogo.querySelector<HTMLButtonElement>('.refresh')!.click();
    expect(dialogo.open).toBe(false);
    http.expectNone({ method: 'DELETE', url: `${url}/${proveedor.id}` });

    eliminar.click();
    fixture.detectChanges();
    dialogo.querySelector<HTMLButtonElement>('.primary')!.click();
    http.expectOne({ method: 'DELETE', url: `${url}/${proveedor.id}` })
      .flush(null, { status: 204, statusText: 'No Content' });
    fixture.detectChanges();
    expect(dialogo.open).toBe(false);
    expect(panel.querySelector('tbody .status')?.textContent).toContain('Inactivo');
    expect(panel.querySelector('button[title="Eliminar"]')).toBeNull();

    panel.querySelector<HTMLButtonElement>('button[title="Reactivar"]')!.click();
    http.expectOne({ method: 'PATCH', url: `${url}/${proveedor.id}/toggle-activo` })
      .flush({ success: true, data: proveedor });
    fixture.detectChanges();
    expect(panel.querySelector('tbody .status')?.textContent).toContain('Activo');
  });

  function listar(datos: Proveedor[]): void {
    http.expectOne({ method: 'GET', url }).flush({ success: true, data: datos });
    fixture.detectChanges();
  }

  function campo(nombre: string): HTMLInputElement {
    return dialogo.querySelector(`[formcontrolname="${nombre}"]`)!;
  }

  function enviar(): void {
    dialogo.querySelector('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    fixture.detectChanges();
  }
});

function escribir(campo: HTMLInputElement, texto: string): void {
  campo.value = texto;
  campo.dispatchEvent(new Event('input'));
}
