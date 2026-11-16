import { CommonModule } from '@angular/common';
import { Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, switchMap } from 'rxjs';
import { toast } from 'ngx-sonner';
import { AdminCatalogoService } from '../../../_services/admin-catalogo.service';
import { MarcaVehiculo } from '../../../models/marca-vehiculo.interface';
import { ModeloVehiculo } from '../../../models/modelo-vehiculo.interface';

@Component({ selector: 'app-marcas-page', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './marcas.html', styleUrl: './marcas.css' })
export class MarcasPage implements OnInit {
  readonly catalogo = inject(AdminCatalogoService);
  marcas: MarcaVehiculo[] = []; modelos: ModeloVehiculo[] = []; busqueda = '';
  vista: 'cuadricula' | 'filas' = 'cuadricula'; cargando = true; error = '';
  fotosFallidas = new Set<string>();
  @ViewChild('editor') private dialog?: ElementRef<HTMLDialogElement>;
  editando: MarcaVehiculo | null = null; nombre = ''; paisOrigen = ''; activo = true;
  archivo: File | null = null; vistaPrevia = ''; guardando = false; errorFormulario = '';
  imagenQuitada = false;
  ngOnInit() { this.cargar(); }
  get visibles() { const q = this.busqueda.trim().toLocaleLowerCase(); return this.marcas.filter(m => !q || `${m.nombre} ${m.paisOrigen || ''}`.toLocaleLowerCase().includes(q)); }
  get activas() { return this.marcas.filter(m => m.activo).length; }
  modelosAsociados(id: string) { return this.modelos.filter(m => m.marcaId === id || m.marca?.id === id).length; }
  nuevo() { this.editando = null; this.nombre = ''; this.paisOrigen = ''; this.activo = true; this.archivo = null; this.vistaPrevia = ''; this.imagenQuitada = false; this.errorFormulario = ''; this.dialog?.nativeElement.showModal(); }
  editar(m: MarcaVehiculo) { this.editando = m; this.nombre = m.nombre; this.paisOrigen = m.paisOrigen || ''; this.activo = m.activo; this.archivo = null; this.vistaPrevia = m.imagenUrl || ''; this.imagenQuitada = false; this.errorFormulario = ''; this.dialog?.nativeElement.showModal(); }
  seleccionar(event: Event) { const file = (event.target as HTMLInputElement).files?.[0]; if (!file) return; if (!['image/png', 'image/jpeg', 'image/webp', 'image/gif'].includes(file.type)) { this.errorFormulario = 'Usa una imagen PNG, JPG, WEBP o GIF.'; return; } if (file.size > 10 * 1024 * 1024) { this.errorFormulario = 'La imagen no puede superar los 10 MB.'; return; } this.archivo = file; this.vistaPrevia = URL.createObjectURL(file); this.errorFormulario = ''; }
  limpiarImagen() { this.archivo = null; this.vistaPrevia = ''; this.imagenQuitada = true; }
  guardar() {
    const datos = { nombre: this.nombre.trim(), paisOrigen: this.paisOrigen.trim(), activo: this.activo, imagenUrl: this.imagenQuitada ? null : this.editando?.imagenUrl || null };
    if (!datos.nombre) { this.errorFormulario = 'El nombre de la marca es obligatorio.'; return; }
    this.guardando = true; this.errorFormulario = '';
    const imagen$ = this.archivo ? this.catalogo.subirImagen(this.archivo, 'marcas') : null;
    const guardar$ = imagen$ ? imagen$.pipe(switchMap(imagen => this.catalogo.guardar<MarcaVehiculo>('marcas-vehiculos', { ...datos, imagenUrl: imagen.url }, this.editando?.id))) : this.catalogo.guardar<MarcaVehiculo>('marcas-vehiculos', datos, this.editando?.id);
    guardar$.pipe(finalize(() => this.guardando = false)).subscribe({ next: marca => { this.marcas = this.editando ? this.marcas.map(item => item.id === marca.id ? marca : item) : [marca, ...this.marcas]; this.dialog?.nativeElement.close(); toast.success(this.editando ? 'Marca actualizada.' : 'Marca creada.'); }, error: e => { this.errorFormulario = e.error?.message || e.message || 'No se pudo guardar la marca.'; toast.error(this.errorFormulario); } });
  }
  cambiarEstado(m: MarcaVehiculo) { this.catalogo.cambiarEstado<MarcaVehiculo>('marcas-vehiculos', m.id).subscribe({ next: actualizada => { this.marcas = this.marcas.map(item => item.id === m.id ? actualizada : item); toast.success(actualizada.activo ? 'Marca activada.' : 'Marca desactivada.'); }, error: e => toast.error(e.error?.message || e.message || 'No se pudo cambiar el estado.') }); }
  cargar() {
    this.cargando = true; this.error = '';
    this.catalogo.listar<MarcaVehiculo>('marcas-vehiculos').pipe(finalize(() => this.cargando = false)).subscribe({
      next: marcas => { this.marcas = marcas; this.catalogo.listar<ModeloVehiculo>('modelos-vehiculos').subscribe({ next: modelos => this.modelos = modelos, error: () => this.modelos = [] }); },
      error: error => this.error = error.error?.message || error.message || 'No pudimos cargar las marcas.',
    });
  }
}
