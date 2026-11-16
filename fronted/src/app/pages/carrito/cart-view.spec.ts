import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CartViewComponent } from './cart-view';
import { CarritoService } from '../../_services/carrito.service';
import { ProductoService } from '../../_services/producto.service';
import { StorageService } from '../../_services/storage.service';

describe('Carrito', () => {
  let page: CartViewComponent;
  let cart: any;
  let storage: any;
  const initial = { id: 'c1', items: [{ id: 'i1', productoId: 'p1', cantidad: 1, precioUnitario: 45, subtotal: 45 }], totalItems: 1, total: 45, moneda: 'PEN' };
  beforeEach(() => {
    cart = { obtenerCarrito: vi.fn(() => of(structuredClone(initial))), actualizarCantidad: vi.fn(() => of({})), eliminarItem: vi.fn(() => of(undefined)), vaciarCarrito: vi.fn(() => of(undefined)) };
    storage = { obtenerUsuarioId: vi.fn(() => 'u1') };
    TestBed.configureTestingModule({ imports: [CartViewComponent], providers: [provideHttpClient(),provideRouter([]),
      { provide: CarritoService, useValue: cart }, { provide: StorageService, useValue: storage },
      { provide: ProductoService, useValue: { obtenerProducto: vi.fn(() => of({ id: 'p1', nombre: 'Aceite', sku: 'P1', activo: true })) } },
    ] });
    page = TestBed.createComponent(CartViewComponent).componentInstance;
    page.ngOnInit();
  });
  it('consulta el carrito de la sesión y completa los productos', () => {
    expect(cart.obtenerCarrito).toHaveBeenCalledWith('u1');
    expect(page.filas[0].producto?.nombre).toBe('Aceite');
    expect(page.carrito?.total).toBe(45);
  });
  it('actualiza cantidad y vuelve a consultar el total del servidor', () => {
    cart.obtenerCarrito.mockReturnValueOnce(of({ ...initial, total: 90, totalItems: 2, items: [{ ...initial.items[0], cantidad: 2, subtotal: 90 }] }));
    page.cambiarCantidad(page.filas[0], 2);
    expect(cart.actualizarCantidad).toHaveBeenCalledWith('c1', 'i1', 2);
    expect(page.carrito?.total).toBe(90); expect(page.ocupado).toBe(false);
  });
  it('rechaza cantidades inválidas', () => {
    page.cambiarCantidad(page.filas[0], 0); page.cambiarCantidad(page.filas[0], 1.5);
    expect(cart.actualizarCantidad).not.toHaveBeenCalled();
  });
  it('elimina una línea y muestra el carrito vacío', () => {
    cart.obtenerCarrito.mockReturnValueOnce(of({ ...initial, items: [], total: 0, totalItems: 0 }));
    page.eliminar(page.filas[0]);
    expect(cart.eliminarItem).toHaveBeenCalledWith('c1', 'i1'); expect(page.filas).toEqual([]);
  });
  it('vacía el carrito del usuario actual', () => {
    page.vaciar(); expect(cart.vaciarCarrito).toHaveBeenCalledWith('u1');
  });
  it('conserva los datos al fallar una actualización y permite reintentar', () => {
    cart.actualizarCantidad.mockReturnValueOnce(throwError(() => new Error('Stock insuficiente')));
    page.cambiarCantidad(page.filas[0], 5);
    expect(page.filas[0].cantidad).toBe(1); expect(page.ocupado).toBe(false);
  });
  it('continúa a datos y bloquea productos no disponibles', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    page.realizarCheckout(); expect(navigate).toHaveBeenCalledWith(['/pago/llenar-datos']);
    navigate.mockClear(); page.filas[0].producto = null; page.realizarCheckout(); expect(navigate).not.toHaveBeenCalled();
  });
  it('solicita iniciar sesión sin consultar un usuario ficticio', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    storage.obtenerUsuarioId.mockReturnValue(null); cart.obtenerCarrito.mockClear(); page.cargar();
    expect(navigate).toHaveBeenCalledWith(['/iniciar-sesion'], { queryParams: { returnUrl: '/carrito' } });
    expect(cart.obtenerCarrito).not.toHaveBeenCalled();
  });
});
