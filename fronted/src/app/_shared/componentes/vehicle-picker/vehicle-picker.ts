import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, Input, OnDestroy, Output, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';

export interface VehiclePickerOption {
  id: string;
  nombre: string;
  imagenUrl?: string | null;
  subtitulo?: string;
}

@Component({
  selector: 'app-vehicle-picker', standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vehicle-picker.html', styleUrl: './vehicle-picker.css',
})
export class VehiclePicker implements OnDestroy {
  @Input() etiqueta = 'Vehículo';
  @Input() placeholder = 'Todos';
  @Input() opciones: VehiclePickerOption[] = [];
  @Input() valor = '';
  @Output() valorChange = new EventEmitter<string>();
  @ViewChild('modal', { static: true }) modal!: ElementRef<HTMLDialogElement>;
  abierto = false;
  busqueda = '';
  termino = '';
  imagenesFallidas = new Set<string>();
  private espera?: ReturnType<typeof setTimeout>;
  private overflowAnterior = '';
  get seleccionada() { return this.opciones.find(o => o.id === this.valor); }
  private normalizar(value: string) { return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim(); }
  get filtradas() {
    const q = this.normalizar(this.termino);
    return this.opciones.filter(o => this.normalizar(o.nombre + ' ' + (o.subtitulo || '')).includes(q));
  }
  abrir(): void {
    clearTimeout(this.espera); this.busqueda = ''; this.termino = ''; this.abierto = true;
    this.imagenesFallidas.clear();
    const body = this.modal.nativeElement.ownerDocument.body;
    this.overflowAnterior = body.style.overflow; body.style.overflow = 'hidden';
    this.modal.nativeElement.showModal();
  }
  cerrar(): void { this.modal.nativeElement.close(); this.cerrado(); }
  cerrado(): void {
    if (this.abierto) this.modal.nativeElement.ownerDocument.body.style.overflow = this.overflowAnterior;
    this.abierto = false; clearTimeout(this.espera);
  }
  buscar(): void {
    clearTimeout(this.espera);
    this.espera = setTimeout(() => this.termino = this.busqueda, 350);
  }
  limpiarBusqueda(): void { clearTimeout(this.espera); this.busqueda = ''; this.termino = ''; }
  confirmarBusqueda(event: Event): void { event.preventDefault(); clearTimeout(this.espera); this.termino = this.busqueda; }
  elegir(id: string): void { this.cerrar(); this.valorChange.emit(id); }
  fondo(event: MouseEvent): void { if (event.target === this.modal.nativeElement) this.cerrar(); }
  ngOnDestroy(): void { clearTimeout(this.espera); if (this.abierto) this.cerrado(); }
}
