import { ConfirmarSalidaService } from '../../../_services/confirmar-salida.service';
import { ParametrosService } from '../../../_services/parametros.service';
import { PerfilService } from '../../../_services/perfil.service';
import { ROLES_GESTION } from '../../../models/rol';
import { PerfilModalService } from '../../../_services/perfil-modal.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, distinctUntilChanged, filter, map, merge, of, startWith, switchMap } from 'rxjs';
import { CarritoService } from '../../../_services/carrito.service';

import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../_services/auth.service';
import { StorageService } from '../../../_services/storage.service';
import { ProductSearchComponent } from '../product-search/product-search';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, ProductSearchComponent],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css']
})
export class NavbarComponent {
 private readonly confirmarSalida=inject(ConfirmarSalidaService);
  readonly parametros=inject(ParametrosService);
  authService = inject(AuthService);
  readonly perfilModal = inject(PerfilModalService);
  private storage = inject(StorageService);
  private router = inject(Router);

  private perfil = inject(PerfilService);
  private carrito = inject(CarritoService);
  readonly cantidadCarrito = signal(0);
  readonly fotoPerfil = signal<string | null>(null);
  readonly fotoFallida = signal(false);

  constructor() {
    merge(this.storage.cambios$, this.router.events.pipe(filter(e => e instanceof NavigationEnd)))
      .pipe(startWith(null), map(() => this.storage.obtenerToken()), distinctUntilChanged(),
        switchMap(token => token ? this.perfil.obtener().pipe(catchError(() => of(null))) : of(null)),
        takeUntilDestroyed()).subscribe();
    merge(this.storage.cambios$, this.carrito.cambios$, this.router.events.pipe(filter(e => e instanceof NavigationEnd)))
      .pipe(startWith(null), switchMap(() => {
        const foto = this.storage.obtenerUsuario()?.fotoPerfilUrl || null;
        if (foto !== this.fotoPerfil()) { this.fotoPerfil.set(foto); this.fotoFallida.set(false); }
        const id = this.storage.obtenerUsuarioId();
        if (!this.storage.obtenerToken() || !id) { this.fotoPerfil.set(null); return of(null); }
        return this.carrito.obtenerCarrito(id).pipe(catchError(() => of(null)));
      }), takeUntilDestroyed()).subscribe(cart => this.cantidadCarrito.set(cart?.totalItems ?? 0));
  }

  menuAbierto = false;

  get haySesion(): boolean {
    return !!this.storage.obtenerToken();
  }

  get puedeGestionar(): boolean { const rol = this.storage.obtenerRol(); return this.haySesion && rol !== null && ROLES_GESTION.includes(rol); }

  alternarMenu(): void {
    this.menuAbierto = !this.menuAbierto;
  }

  cerrarMenu(): void {
    this.menuAbierto = false;
  }

  cerrarSesion(): void {
    this.cerrarMenu();
    this.confirmarSalida.solicitar(()=>{this.authService.logout();void this.router.navigate(['/']);});
  }
}
