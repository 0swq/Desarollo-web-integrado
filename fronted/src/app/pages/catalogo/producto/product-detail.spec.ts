import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ProductDetail } from './product-detail';
import { ProductoDetalleData, ProductoDetalleService } from '../../../_services/producto-detalle.service';
import { CarritoService } from '../../../_services/carrito.service';
import { StorageService } from '../../../_services/storage.service';

const detalle: ProductoDetalleData = {
  producto: { id: 'p1', sku: 'DEMO-P1', nombre: 'Aceite', precio: 45, precioPromocional: 39.9, activo: true },
  stock: { productoId: 'p1', cantidad: 5 }, categorias: [], compatibilidades: [], modelos: [], marcas: [], otros: [], datosIncompletos: false,
};

describe('Detalle de producto', () => {
  let fixture: ComponentFixture<ProductDetail>;
  let page: ProductDetail;
  let params: BehaviorSubject<ReturnType<typeof convertToParamMap>>;
  let service: { obtener: ReturnType<typeof vi.fn> };
  let cart: { obtenerCarrito: ReturnType<typeof vi.fn>; agregarItem: ReturnType<typeof vi.fn> };
  let storage: { obtenerUsuarioId: ReturnType<typeof vi.fn>; obtenerToken: ReturnType<typeof vi.fn> };
  beforeEach(() => {
    params = new BehaviorSubject(convertToParamMap({ id: 'p1' }));
    service = { obtener: vi.fn(() => of(structuredClone(detalle))) };
    cart = { obtenerCarrito: vi.fn(() => of({ id: 'cart1' })), agregarItem: vi.fn(() => of({ success: true })) };
    storage = { obtenerUsuarioId: vi.fn(() => 'u1'), obtenerToken: vi.fn(() => 'token') };
    TestBed.configureTestingModule({ imports: [ProductDetail], providers: [provideRouter([]),
      { provide: ActivatedRoute, useValue: { paramMap: params } },
      { provide: ProductoDetalleService, useValue: service },
      { provide: CarritoService, useValue: cart }, { provide: StorageService, useValue: storage },
    ] });
    fixture = TestBed.createComponent(ProductDetail); page = fixture.componentInstance;
  });
  it('carga la ruta y muestra precio promocional y disponibilidad reales', () => {
    expect(service.obtener).toHaveBeenCalledWith('p1');
    expect(page.precio).toBe(39.9); expect(page.disponible).toBe(true); expect(page.cargando).toBe(false);
  });
  it('recarga al cambiar de producto y reinicia cantidad y pestaña', () => {
    page.cantidad = 3; page.tab = 'ficha'; page.imagenFallida = true;
    params.next(convertToParamMap({ id: 'p2' }));
    expect(service.obtener).toHaveBeenLastCalledWith('p2');
    expect(page.cantidad).toBe(1); expect(page.tab).toBe('descripcion'); expect(page.imagenFallida).toBe(false);
  });
  it('distingue un 404 y permite reintentar después de un fallo de conexión', () => {
    service.obtener.mockReturnValueOnce(throwError(() => ({ status: 404 })));
    params.next(convertToParamMap({ id: 'no-existe' }));
    expect(page.noEncontrado).toBe(true);
    service.obtener.mockReturnValueOnce(throwError(() => ({ status: 0 })));
    params.next(convertToParamMap({ id: 'p1' }));
    expect(page.error).not.toBe(''); page.reintentar();
    expect(page.error).toBe(''); expect(page.producto?.id).toBe('p1');
  });
  it('añade la cantidad solicitada al carrito del usuario', () => {
    page.cantidad = 2; page.agregar();
    expect(cart.obtenerCarrito).toHaveBeenCalledWith('u1');
    expect(cart.agregarItem).toHaveBeenCalledWith('cart1', 'p1', 2);
    expect(page.agregado).toBe(true); expect(page.agregando).toBe(false);
  });
  it('bloquea artículos agotados y cantidades superiores al stock', () => {
    page.cantidad = 6; page.agregar(); expect(cart.obtenerCarrito).not.toHaveBeenCalled();
    page.cantidad = 1; page.detalle!.stock!.cantidad = 0; page.agregar();
    expect(page.disponible).toBe(false); expect(cart.obtenerCarrito).not.toHaveBeenCalled();
  });
  it('solicita iniciar sesión y conserva la dirección del producto', () => {
    storage.obtenerUsuarioId.mockReturnValue(null);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    page.agregar(); expect(navigate).toHaveBeenCalledWith(['/iniciar-sesion'], expect.objectContaining({ queryParams: expect.objectContaining({ returnUrl: expect.any(String) }) }));
    expect(cart.obtenerCarrito).not.toHaveBeenCalled();
  });
});
