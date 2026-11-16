import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { BehaviorSubject, of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { CatalogData, CatalogoService } from '../../../_services/catalogo.service';
import { ProductListComponent } from './product-list';

const datos: CatalogData = {
  productos: [
    { id: 'p1', sku: 'F-001', nombre: 'Filtro aceite', descripcion: 'Motor protegido', precio: 100, precioPromocional: 80, moneda: 'PEN', proveedorId: 'v1', activo: true },
    { id: 'p2', sku: 'F-002', nombre: 'Filtro aire', precio: 60, moneda: 'PEN', proveedorId: 'v2', activo: true },
    { id: 'p3', sku: 'X-003', nombre: 'Oculto', precio: 10, activo: false },
  ],
  categorias: [{ id: 'c1', nombre: 'Filtros', activo: true }],
  marcas: [{ id: 'b1', nombre: 'Toyota', activo: true }],
  modelos: [{ id: 'm1', nombre: 'Corolla', marcaId: 'b1', tipoVehiculo: 'Sedán', activo: true }],
  compatibilidades: [{ id: 'fit1', productoId: 'p1', modeloVehiculoId: 'm1', anioInicio: 2018, anioFin: 2022, motor: '1.8' }],
  proveedores: [], stocks: [{ productoId: 'p1', cantidad: 5 }, { productoId: 'p2', cantidad: 0 }],
  relaciones: [{ productoId: 'p1', categoriaId: 'c1' }],
};

describe('Filtros combinados del catálogo', () => {
  let component: ProductListComponent;
  let params: BehaviorSubject<ReturnType<typeof convertToParamMap>>;
  afterEach(() => vi.useRealTimers());
  beforeEach(() => {
    params = new BehaviorSubject(convertToParamMap({}));
    TestBed.configureTestingModule({ providers: [provideHttpClient(),
      { provide: ActivatedRoute, useValue: { queryParamMap: params } },
      { provide: Router, useValue: { navigate: vi.fn().mockResolvedValue(true) } },
      { provide: CatalogoService, useValue: { cargar: () => of(structuredClone(datos)) } },
    ] });
    component = TestBed.createComponent(ProductListComponent).componentInstance;
  });
  it('cruza categoría, marca, modelo, año, motor, proveedor y disponibilidad', () => {
    params.next(convertToParamMap({ categoria: 'c1', marca: 'b1', modelo: 'm1', anio: '2020', motor: '1.8', proveedor: 'v1', stock: 'true' }));
    expect(component.resultados.map(p => p.id)).toEqual(['p1']);
    params.next(convertToParamMap({ modelo: 'm1', anio: '2023' }));
    expect(component.resultados).toEqual([]);
  });
  it('busca SKU exacto sin distinguir mayúsculas y excluye productos inactivos', () => {
    params.next(convertToParamMap({ sku: 'f-001' }));
    expect(component.resultados.map(p => p.id)).toEqual(['p1']);
    params.next(convertToParamMap({ sku: 'F-00' }));
    expect(component.resultados).toEqual([]);
    params.next(convertToParamMap({}));
    expect(component.resultados).toHaveLength(2);
  });
  it('filtra y ordena usando el precio promocional efectivo', () => {
    params.next(convertToParamMap({ minimo: '70', maximo: '90', oferta: 'true', moneda: 'PEN' }));
    expect(component.resultados.map(p => p.id)).toEqual(['p1']);
    params.next(convertToParamMap({ orden: 'precio-asc' }));
    expect(component.resultados.map(p => p.id)).toEqual(['p2', 'p1']);
  });
  it('combina la búsqueda de texto con categoría y detecta precios invertidos', () => {
    params.next(convertToParamMap({ q: 'motor', categoria: 'c1' }));
    expect(component.resultados.map(p => p.id)).toEqual(['p1']);
    params.next(convertToParamMap({ minimo: '90', maximo: '10' }));
    expect(component.rangoInvalido).toBe(true);
  });
  it('pagina resultados y ajusta páginas fuera de rango', () => {
    component.data.productos = Array.from({ length: 30 }, (_, i) => ({ ...datos.productos[0], id: `p${i}`, sku: `S${i}` }));
    params.next(convertToParamMap({ pagina: '3', cantidad: '12' }));
    expect(component.visibles).toHaveLength(6);
    expect(component.desde).toBe(25);
    params.next(convertToParamMap({ pagina: '99', cantidad: '12' }));
    expect(component.pagina).toBe(3);
  });
  it('espera 350 ms tras la última pulsación antes de aplicar texto o precio', () => {
    vi.useFakeTimers();
    const navigate = vi.mocked(TestBed.inject(Router).navigate);
    component.filtros.q = 'fil';
    component.actualizarFiltros(true);
    vi.advanceTimersByTime(200);
    component.filtros.q = 'filtro';
    component.actualizarFiltros(true);
    vi.advanceTimersByTime(349);
    expect(navigate).not.toHaveBeenCalled();
    vi.advanceTimersByTime(1);
    expect(navigate).toHaveBeenCalledExactlyOnceWith([], expect.objectContaining({
      queryParams: expect.objectContaining({ q: 'filtro', pagina: 1 }),
    }));
  });
  it('aplica selecciones inmediatamente y cancela la espera pendiente', () => {
    vi.useFakeTimers();
    const navigate = vi.mocked(TestBed.inject(Router).navigate);
    component.actualizarFiltros(true);
    component.filtros.categoria = 'c1';
    component.filtrosAbiertos = true;
    component.actualizarFiltros();
    expect(navigate).toHaveBeenCalledTimes(1);
    expect(component.filtrosAbiertos).toBe(true);
    vi.advanceTimersByTime(400);
    expect(navigate).toHaveBeenCalledTimes(1);
  });
  it('vaciar un precio no lo convierte en un límite de cero', () => {
    component.filtros.maximo = null as unknown as string;
    component.actualizarFiltros();
    expect(component.filtros.maximo).toBe('');
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith([], expect.objectContaining({
      queryParams: expect.objectContaining({ maximo: null }),
    }));
  });
  it('seleccionar un modelo también selecciona su marca y limpia el motor', () => {
    component.filtros.motor = '1.8';
    component.seleccionarModelo('m1');
    expect(component.filtros.marca).toBe('b1'); expect(component.filtros.modelo).toBe('m1'); expect(component.filtros.motor).toBe('');
    expect(TestBed.inject(Router).navigate).toHaveBeenCalledWith([], expect.objectContaining({ queryParams: expect.objectContaining({ marca: 'b1', modelo: 'm1' }) }));
  });
  it('cambiar de marca elimina el modelo anterior y actualiza las opciones disponibles', () => {
    component.filtros.modelo = 'm1'; component.filtros.motor = '1.8';
    component.seleccionarMarca('otra');
    expect(component.filtros.modelo).toBe(''); expect(component.filtros.motor).toBe(''); expect(component.opcionesModelos).toEqual([]);
  });
});
