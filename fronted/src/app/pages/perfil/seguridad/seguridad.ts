import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { toast } from 'ngx-sonner';
import { PerfilService } from '../../../_services/perfil.service';
function coinciden(a: string, b: string) { return (form: AbstractControl): ValidationErrors | null => form.get(a)?.value === form.get(b)?.value ? null : { noCoinciden: true }; }
@Component({ selector: 'app-seguridad-page', standalone: true, imports: [ReactiveFormsModule], templateUrl: './seguridad.html', styleUrl: './seguridad.css' })
export class SeguridadPage implements OnInit {
  private service = inject(PerfilService); private fb = inject(FormBuilder); private destroyRef = inject(DestroyRef);
  correoActual = ''; cargando = true; errorCarga = ''; ocupado = ''; errorPassword = ''; errorCorreo = ''; verPasswords = false;
  password = this.fb.nonNullable.group({ contrasenaActual: ['', [Validators.required, Validators.maxLength(200)]], contrasenaNueva: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72), Validators.pattern(/.*\S.*/)]], confirmacionContrasena: ['', [Validators.required, Validators.maxLength(72)]] }, { validators: coinciden('contrasenaNueva', 'confirmacionContrasena') });
  correo = this.fb.nonNullable.group({ correoNuevo: ['', [Validators.required, Validators.email, Validators.maxLength(254)]], confirmacionCorreo: ['', [Validators.required, Validators.email, Validators.maxLength(254)]], contrasenaActual: ['', [Validators.required, Validators.maxLength(200)]] }, { validators: control => {
    const a = control.get('correoNuevo')?.value.trim().toLowerCase(); const b = control.get('confirmacionCorreo')?.value.trim().toLowerCase(); return a === b ? null : { noCoinciden: true };
  } });
  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.cargando = true; this.errorCarga = ''; this.service.obtener().pipe(finalize(() => this.cargando = false), takeUntilDestroyed(this.destroyRef)).subscribe({ next: u => this.correoActual = u.correo, error: () => this.errorCarga = 'No pudimos consultar tu cuenta. Vuelve a intentarlo.' }); }
  private bloquear(tipo: string): void { this.ocupado = tipo; this.password.disable(); this.correo.disable(); }
  private liberar(): void { this.ocupado = ''; this.password.enable(); this.correo.enable(); }
  guardarPassword(): void {
    if (this.ocupado) return; this.errorPassword = ''; this.password.markAllAsTouched(); if (this.password.invalid) return;
    const datos = this.password.getRawValue();
    if (new TextEncoder().encode(datos.contrasenaNueva).length > 72) { this.errorPassword = 'La contraseña es demasiado larga. Usa menos caracteres especiales.'; return; }
    if (datos.contrasenaActual === datos.contrasenaNueva) { this.errorPassword = 'La nueva contraseña debe ser diferente de la actual.'; return; }
    this.bloquear('password');
    this.service.cambiarPassword(datos).pipe(finalize(() => this.liberar()), takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.password.reset(); this.verPasswords = false; toast.success('Contraseña actualizada. Usa la nueva contraseña al iniciar sesión.'); },
      error: e => { this.errorPassword = e.error?.message || e.message || 'No se pudo cambiar la contraseña.'; toast.error(this.errorPassword); },
    });
  }
  guardarCorreo(): void {
    if (this.ocupado) return; this.errorCorreo = ''; this.correo.markAllAsTouched(); if (this.correo.invalid) return;
    const datos = this.correo.getRawValue(); datos.correoNuevo = datos.correoNuevo.trim().toLowerCase(); datos.confirmacionCorreo = datos.confirmacionCorreo.trim().toLowerCase();
    if (datos.correoNuevo === this.correoActual.toLowerCase()) { this.errorCorreo = 'El nuevo correo debe ser diferente del actual.'; return; }
    this.bloquear('correo');
    this.service.cambiarCorreo(datos).pipe(finalize(() => this.liberar()), takeUntilDestroyed(this.destroyRef)).subscribe({
      next: sesion => { this.correoActual = sesion.correo; this.correo.reset(); toast.success('Correo actualizado. Tu sesión continúa activa.'); },
      error: e => { this.errorCorreo = e.error?.message || e.message || 'No se pudo cambiar el correo.'; toast.error(this.errorCorreo); },
    });
  }
}
