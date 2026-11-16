import { DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../_services/auth.service';
import { toast } from 'ngx-sonner';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loginForm = new FormGroup({
    correo: new FormControl('', [Validators.required, Validators.email]),
    contrasena: new FormControl('', [Validators.required, Validators.minLength(6)])
  });

  private destroy = inject(DestroyRef);
  cargando = false;
  mostrarPassword = false;
  enviado = false;
  errorMensaje = '';

  get form() {
    return this.loginForm.controls;
  }

  onSubmit() {
    if (this.cargando) return;
    this.enviado = true;
    this.loginForm.markAllAsTouched();
    if (this.loginForm.invalid) {
      return;
    }

    const valores = this.loginForm.getRawValue() as { correo: string; contrasena: string };
    this.errorMensaje = '';

    this.cargando = true;
    valores.correo = valores.correo.trim().toLowerCase();
    this.authService.login(valores).pipe(finalize(() => this.cargando = false), takeUntilDestroyed(this.destroy)).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          toast.success(res.message || 'Sesión iniciada correctamente');
          const regreso = this.route.snapshot.queryParamMap.get('returnUrl');
          void this.router.navigateByUrl(regreso?.startsWith('/') && !regreso.startsWith('//') ? regreso : '/');
        } else {
          this.errorMensaje = res.message || 'No se pudo iniciar sesión.';
        }
      },
      error: (err) => {
        this.errorMensaje = err.status === 0
          ? 'No se pudo conectar con el servidor. Verifica que el backend esté iniciado.'
          : err.error?.message || 'Correo o contraseña incorrectos.';
        toast.error(this.errorMensaje);
      }
    });
  }

  get regreso(): string | null { return this.route.snapshot.queryParamMap.get('returnUrl'); }

  onReset() {
    this.enviado = false;
    this.loginForm.reset();
  }
}
