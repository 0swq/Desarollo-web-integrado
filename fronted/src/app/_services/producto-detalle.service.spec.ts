import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { of } from 'rxjs';
import { ProductoDetalleData, ProductoDetalleService } from './producto-detalle.service';
import { ProductoService } from './producto.service';

 describe('Datos de la ficha de producto', () => {
  let http: HttpTestingController;
  let service: ProductoDetalleService;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), {
      provide: ProductoService, useValue: { obtenerProducto: () => of({ id: 'p1', nombre: 'Aceite', sku: 'A1', precio: 45, activo: true }) },
    }] });
    http = TestBed.inject(HttpTestingController); service = TestBed.inject(ProductoDetalleService);
  });
  afterEach(() => http.verify());
  it('consulta stock por la ruta pública y selecciona el producto correcto', () => {
    let data: ProductoDetalleData | undefined;
    service.obtener('p1').subscribe(value => data = value);
    http.expectNone(req => req.url.includes('/stocks/producto/'));
    const requests = http.match(() => true);
    expect(requests.some(req => req.request.url.endsWith('/stocks'))).toBe(true);
    for (const request of requests) request.flush({ success: true, data: request.request.url.endsWith('/stocks') ? [{ productoId: 'p2', cantidad: 99 }, { productoId: 'p1', cantidad: 7 }] : [] });
    expect(data?.stock?.cantidad).toBe(7); expect(data?.datosIncompletos).toBe(false);
  });
  it('conserva el producto si falla información adicional y avisa al usuario', () => {
    let data: ProductoDetalleData | undefined;
    service.obtener('p1').subscribe(value => data = value);
    for (const request of http.match(() => true)) {
      if (request.request.url.endsWith('/stocks')) request.flush({}, { status: 503, statusText: 'Unavailable' });
      else request.flush({ success: true, data: [] });
    }
    expect(data?.producto.id).toBe('p1'); expect(data?.stock).toBeNull(); expect(data?.datosIncompletos).toBe(true);
  });
});
