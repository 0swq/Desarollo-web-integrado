import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { VehiclePicker } from './vehicle-picker';

describe('Selector visual de vehículos', () => {
  let fixture: ComponentFixture<VehiclePicker>;
  let picker: VehiclePicker;
  let dialog: HTMLDialogElement;
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [VehiclePicker] });
    fixture = TestBed.createComponent(VehiclePicker); picker = fixture.componentInstance;
    picker.etiqueta = 'Modelo'; picker.placeholder = 'Todos los modelos';
    picker.opciones = [{ id: '1', nombre: 'Corolla Sedán', imagenUrl: '/api/storage/imagenes/modelos/a.png', subtitulo: 'Toyota · Sedán' }, { id: '2', nombre: 'Swift', subtitulo: 'Suzuki · Hatchback' }];
    fixture.detectChanges(); dialog = picker.modal.nativeElement;
    dialog.showModal = vi.fn(); dialog.close = vi.fn();
  });
  afterEach(() => { fixture.destroy(); vi.useRealTimers(); });
  it('abre el modal y permite seleccionar la tarjeta con foto', () => {
    picker.abrir(); fixture.detectChanges();
    expect(dialog.showModal).toHaveBeenCalledOnce(); expect(picker.abierto).toBe(true);
    expect(fixture.nativeElement.querySelector('.option-card img').getAttribute('src')).toContain('/modelos/a.png');
    const changed = vi.spyOn(picker.valorChange, 'emit');
    fixture.nativeElement.querySelectorAll('.option-card')[1].click();
    expect(changed).toHaveBeenCalledWith('1'); expect(picker.abierto).toBe(false); expect(dialog.close).toHaveBeenCalledOnce();
  });
  it('busca nombres o marcas sin distinguir tildes tras 350 ms', () => {
    vi.useFakeTimers(); picker.busqueda = 'sedan'; picker.buscar();
    vi.advanceTimersByTime(349); expect(picker.filtradas).toHaveLength(2);
    vi.advanceTimersByTime(1); expect(picker.filtradas.map(x => x.id)).toEqual(['1']);
    picker.busqueda = 'suzuki'; picker.buscar(); vi.advanceTimersByTime(350);
    expect(picker.filtradas.map(x => x.id)).toEqual(['2']);
  });
  it('Enter busca dentro del modal sin enviar el formulario del catálogo', () => {
    const enter = new KeyboardEvent('keydown', { key: 'Enter', cancelable: true });
    picker.busqueda = 'Toyota'; picker.confirmarBusqueda(enter);
    expect(enter.defaultPrevented).toBe(true); expect(picker.filtradas).toHaveLength(1);
  });
  it('cancelar cierra el estado y restaura el scroll de la página', () => {
    picker.abrir(); expect(dialog.ownerDocument.body.style.overflow).toBe('hidden');
    dialog.dispatchEvent(new Event('cancel')); expect(picker.abierto).toBe(false);
    expect(dialog.ownerDocument.body.style.overflow).not.toBe('hidden');
  });
});
