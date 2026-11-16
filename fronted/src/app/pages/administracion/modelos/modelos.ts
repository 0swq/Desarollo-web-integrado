import { CommonModule } from '@angular/common';
import { Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, switchMap } from 'rxjs';
import { toast } from 'ngx-sonner';
import { AdminCatalogoService } from '../../../_services/admin-catalogo.service';
import { MarcaVehiculo } from '../../../models/marca-vehiculo.interface';
import { ModeloVehiculo } from '../../../models/modelo-vehiculo.interface';

@Component({ selector: 'app-modelos-page', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './modelos.html', styleUrl: './modelos.css' })
export class ModelosPage implements OnInit {
  readonly catalogo = inject(AdminCatalogoService);
  modelos: ModeloVehiculo[] = []; marcas: MarcaVehiculo[] = []; busqueda = ''; marcaFiltro = '';
  vista: 'cuadricula' | 'filas' = 'cuadricula'; cargando = true; error = '';
  fotosFallidas = new Set<string>();
  @ViewChild('editor') private dialog?: ElementRef<HTMLDialogElement>;
  editando: ModeloVehiculo | null = null; nombre = ''; marcaId = ''; tipoVehiculo = ''; activo = true;
  archivo: File | null = null; vistaPrevia = ''; guardando = false; errorFormulario = '';
  imagenQuitada = false;
  ngOnInit() { this.cargar(); }
  get visibles() { const q = this.busqueda.trim().toLocaleLowerCase(); return this.modelos.filter(m => (!this.marcaFiltro || (m.marcaId || m.marca?.id) === this.marcaFiltro) && (!q || `${m.nombre} ${m.marca?.nombre || this.nombreMarca(m.marcaId)} ${m.tipoVehiculo || ''}`.toLocaleLowerCase().includes(q))); }
  get activas() { return this.modelos.filter(m => m.activo).length; }
  nombreMarca(id?: string | null) { return this.marcas.find(m => m.id === id)?.nombre || 'Marca sin asignar'; }
  nuevo() { this.editando = null; this.nombre = ''; this.marcaId = ''; this.tipoVehiculo = ''; this.activo = true; this.archivo = null; this.vistaPrevia = ''; this.imagenQuitada = false; this.errorFormulario = ''; this.dialog?.nativeElement.showModal(); }
  editar(m: ModeloVehiculo) { this.editando = m; this.nombre = m.nombre; this.marcaId = m.marcaId || m.marca?.id || ''; this.tipoVehiculo = m.tipoVehiculo || ''; this.activo = m.activo; this.archivo = null; this.vistaPrevia = m.imagenUrl || ''; this.imagenQuitada = false; this.errorFormulario = ''; this.dialog?.nativeElement.showModal(); }
  seleccionar(event: Event) { const file = (event.target as HTMLInputElement).files?.[0]; if (!file) return; if (!['image/png', 'image/jpeg', 'image/webp', 'image/gif'].includes(file.type)) { this.errorFormulario = 'Usa una imagen PNG, JPG, WEBP o GIF.'; return; } if (file.size > 10 * 1024 * 1024) { this.errorFormulario = 'La imagen no puede superar los 10 MB.'; return; } this.archivo = file; this.vistaPrevia = URL.createObjectURL(file); this.errorFormulario = ''; }
  limpiarImagen() { this.archivo = null; this.vistaPrevia = ''; this.imagenQuitada = true; }
  guardar() {
    if (!this.nombre.trim() || !this.marcaId) { this.errorFormulario = 'Completa el nombre y selecciona una marca.'; return; }
    const datos = { nombre: this.nombre.trim(), marcaId: this.marcaId, tipoVehiculo: this.tipoVehiculo.trim(), activo: this.activo, imagenUrl: this.imagenQuitada ? null : this.editando?.imagenUrl || null };
    this.guardando = true; this.errorFormulario = '';
    const imagen$ = this.archivo ? this.catalogo.subirImagen(this.archivo, 'modelos') : null;
    const guardar$ = imagen$ ? imagen$.pipe(switchMap(imagen => this.catalogo.guardar<ModeloVehiculo>('modelos-vehiculos', { ...datos, imagenUrl: imagen.url }, this.editando?.id))) : this.catalogo.guardar<ModeloVehiculo>('modelos-vehiculos', datos, this.editando?.id);
    guardar$.pipe(finalize(() => this.guardando = false)).subscribe({ next: modelo => { this.modelos = this.editando ? this.modelos.map(item => item.id === modelo.id ? modelo : item) : [modelo, ...this.modelos]; this.dialog?.nativeElement.close(); toast.success(this.editando ? 'Modelo actualizado.' : 'Modelo creado.'); }, error: e => { this.errorFormulario = e.error?.message || e.message || 'No se pudo guardar el modelo.'; toast.error(this.errorFormulario); } });
  }
  cambiarEstado(m: ModeloVehiculo) { this.catalogo.cambiarEstado<ModeloVehiculo>('modelos-vehiculos', m.id).subscribe({ next: actualizado => { this.modelos = this.modelos.map(item => item.id === m.id ? actualizado : item); toast.success(actualizado.activo ? 'Modelo activado.' : 'Modelo desactivado.'); }, error: e => toast.error(e.error?.message || e.message || 'No se pudo cambiar el estado.') }); }
  cargar() {
    this.cargando = true; this.error = '';
    this.catalogo.listar<ModeloVehiculo>('modelos-vehiculos').pipe(finalize(() => this.cargando = false)).subscribe({ next: modelos => this.modelos = modelos, error: error => this.error = error.error?.message || error.message || 'No pudimos cargar los modelos.' });
    this.catalogo.listar<MarcaVehiculo>('marcas-vehiculos').subscribe({ next: marcas => this.marcas = marcas, error: () => this.marcas = [] });
  }
}
