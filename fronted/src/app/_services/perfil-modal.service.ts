import { Injectable, inject, signal } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { StorageService } from './storage.service';
export type SeccionPerfil = 'mi-perfil' | 'mis-pedidos' | 'seguridad';
@Injectable({ providedIn: 'root' })
export class PerfilModalService {
  private storage = inject(StorageService);
  private router = inject(Router);
  readonly seccion = signal<SeccionPerfil>('mi-perfil');
  readonly abierto = signal(false);
  constructor() {
    this.router.events.pipe(filter(e => e instanceof NavigationEnd), takeUntilDestroyed()).subscribe(() => this.sincronizar());
    this.sincronizar();
  }
  private sincronizar(): void {
    const valor = this.router.parseUrl(this.router.url).queryParams['perfil'];
    const valida = ['mi-perfil', 'mis-pedidos', 'seguridad'].includes(valor);
    if (valida) this.seccion.set(valor as SeccionPerfil);
    this.abierto.set(valida && !!this.storage.obtenerToken());
  }
  abrir(seccion: SeccionPerfil = 'mi-perfil'): Promise<boolean> {
    if (!this.storage.obtenerToken()) return this.router.navigate(['/iniciar-sesion']);
    const url = this.router.parseUrl(this.router.url);
    url.queryParams = { ...url.queryParams, perfil: seccion };
    return this.router.navigateByUrl(url);
  }
  cerrar(): Promise<boolean> {
    this.abierto.set(false);
    const url = this.router.parseUrl(this.router.url);
    if (!('perfil' in url.queryParams)) return Promise.resolve(true);
    delete url.queryParams['perfil'];
    return this.router.navigateByUrl(url);
  }
}
