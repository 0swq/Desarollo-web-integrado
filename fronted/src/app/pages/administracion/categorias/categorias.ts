import { CommonModule } from '@angular/common';
import { Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { toast } from 'ngx-sonner';
import { AdminCatalogoService } from '../../../_services/admin-catalogo.service';
import { Categoria } from '../../../models/categoria.interface';

@Component({ selector: 'app-categorias-page', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './categorias.html', styleUrl: './categorias.css' })
export class CategoriasPage implements OnInit {
  readonly catalogo = inject(AdminCatalogoService);
  categorias: Categoria[] = []; busqueda = ''; cargando = true; error = '';
  @ViewChild('editor') private dialog?: ElementRef<HTMLDialogElement>;
  editando: Categoria | null = null; nombre = ''; descripcion = ''; activo = true;
  guardando = false; errorFormulario = '';
  ngOnInit() { this.cargar(); }
  get visibles() { const q = this.busqueda.trim().toLocaleLowerCase(); return this.categorias.filter(c => !q || `${c.nombre} ${c.descripcion || ''}`.toLocaleLowerCase().includes(q)); }
  get activas() { return this.categorias.filter(c => c.activo).length; }
  nuevo() { this.editando = null; this.nombre = ''; this.descripcion = ''; this.activo = true; this.errorFormulario = ''; this.dialog?.nativeElement.showModal(); }
  editar(c: Categoria) { this.editando = c; this.nombre = c.nombre; this.descripcion = c.descripcion || ''; this.activo = c.activo; this.errorFormulario = ''; this.dialog?.nativeElement.showModal(); }
  guardar() {
    if (!this.nombre.trim()) { this.errorFormulario = 'El nombre de la categoría es obligatorio.'; return; }
    const datos = { nombre: this.nombre.trim(), descripcion: this.descripcion.trim(), activo: this.activo };
    this.guardando = true; this.errorFormulario = '';
    this.catalogo.guardar<Categoria>('categorias', datos, this.editando?.id).pipe(finalize(() => this.guardando = false)).subscribe({ next: categoria => { this.categorias = this.editando ? this.categorias.map(item => item.id === categoria.id ? categoria : item) : [categoria, ...this.categorias]; this.dialog?.nativeElement.close(); toast.success(this.editando ? 'Categoría actualizada.' : 'Categoría creada.'); }, error: e => { this.errorFormulario = e.error?.message || e.message || 'No se pudo guardar la categoría.'; toast.error(this.errorFormulario); } });
  }
  cambiarEstado(c: Categoria) { this.catalogo.cambiarEstado<Categoria>('categorias', c.id).subscribe({ next: actualizada => { this.categorias = this.categorias.map(item => item.id === c.id ? actualizada : item); toast.success(actualizada.activo ? 'Categoría activada.' : 'Categoría desactivada.'); }, error: e => toast.error(e.error?.message || e.message || 'No se pudo cambiar el estado.') }); }
  cargar() {
    this.cargando = true; this.error = '';
    this.catalogo.listar<Categoria>('categorias').pipe(finalize(() => this.cargando = false)).subscribe({ next: categorias => this.categorias = categorias, error: error => this.error = error.error?.message || error.message || 'No pudimos cargar las categorías.' });
  }
}
