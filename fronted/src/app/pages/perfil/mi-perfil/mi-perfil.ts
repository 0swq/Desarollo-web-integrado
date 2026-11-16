import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { toast } from 'ngx-sonner';
import { PerfilService } from '../../../_services/perfil.service';
import { UsuarioResponse } from '../../../models/auth.interface';
@Component({ selector: 'app-mi-perfil-page', standalone: true, imports: [CommonModule, ReactiveFormsModule], templateUrl: './mi-perfil.html', styleUrl: './mi-perfil.css' })
export class MiPerfilPage implements OnInit {
  private service = inject(PerfilService); private destroyRef = inject(DestroyRef); private fb = inject(FormBuilder);
  usuario: UsuarioResponse | null = null; cargando = true; guardando = false; error = ''; fotoFallida = false;
  form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/.*\S.*/)]],
    apellido: ['', [Validators.required, Validators.maxLength(100), Validators.pattern(/.*\S.*/)]],
    telefono: ['', [Validators.maxLength(20), Validators.pattern(/^[0-9+() -]*$/)]],
    direccion: ['', Validators.maxLength(255)],
    fotoPerfilUrl: ['', [Validators.maxLength(2048), Validators.pattern(/^(https?:\/\/[^\s]+|\/api\/storage\/imagenes\/[^\s]+)?$/)]],
  });
  ngOnInit(): void { this.cargar(); }
  private mostrar(u: UsuarioResponse): void { this.usuario = u; this.fotoFallida = false; this.form.reset({ nombre: u.nombre, apellido: u.apellido, telefono: u.telefono || '', direccion: u.direccion || '', fotoPerfilUrl: u.fotoPerfilUrl || '' }); }
  cargar(): void { this.cargando = true; this.error = ''; this.service.obtener().pipe(finalize(() => this.cargando = false), takeUntilDestroyed(this.destroyRef)).subscribe({ next: u => this.mostrar(u), error: () => this.error = 'No pudimos cargar tu perfil. Inténtalo nuevamente.' }); }
  guardar(): void {
    if (this.guardando) return; this.form.markAllAsTouched(); if (this.form.invalid) return;
    const datos = this.form.getRawValue(); for (const key of Object.keys(datos) as (keyof typeof datos)[]) datos[key] = datos[key].trim();
    this.guardando = true; this.form.disable();
    this.service.guardar(datos).pipe(finalize(() => { this.guardando = false; this.form.enable(); }), takeUntilDestroyed(this.destroyRef)).subscribe({ next: u => { this.mostrar(u); toast.success('Tus datos se actualizaron.'); }, error: e => toast.error(e.error?.message || e.message || 'No se pudo guardar tu perfil.') });
  }
  subirFoto(event: Event): void {
    const input = event.target as HTMLInputElement; const archivo = input.files?.[0]; input.value = '';
    if (!archivo || this.guardando) return;
    if (!['image/jpeg','image/png','image/webp','image/gif'].includes(archivo.type) || archivo.size > 10 * 1024 * 1024) { toast.error('Elige una imagen JPG, PNG, WebP o GIF de hasta 10 MB.'); return; }
    this.guardando = true; this.form.disable();
    this.service.subirFoto(archivo).pipe(finalize(() => { this.guardando = false; this.form.enable(); }), takeUntilDestroyed(this.destroyRef)).subscribe({ next: u => { this.usuario = u; this.fotoFallida = false; this.form.controls.fotoPerfilUrl.setValue(u.fotoPerfilUrl || ''); toast.success('Foto de perfil actualizada.'); }, error: e => toast.error(e.error?.message || 'No se pudo subir la foto.') });
  }
  cancelar(): void { if (this.usuario && !this.guardando) this.mostrar(this.usuario); }
}
