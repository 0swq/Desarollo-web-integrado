import { ConfirmarSalidaService } from '../../../_services/confirmar-salida.service';
import { ParametrosService } from '../../../_services/parametros.service';
import { PerfilModalService } from '../../../_services/perfil-modal.service';
import { Component, inject, input } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { StorageService } from '../../../_services/storage.service';

import { ROLES_GESTION } from '../../../models/rol';

@Component({
  selector: 'app-footer', standalone: true, imports: [RouterModule],
  templateUrl: './footer.html', styleUrl: './footer.css',
})
export class Footer {
 private readonly confirmarSalida=inject(ConfirmarSalidaService);
  readonly compacto = input(false);
  readonly perfilModal = inject(PerfilModalService);
  private storage = inject(StorageService);
  private router = inject(Router);
  private parametros = inject(ParametrosService);
  get contacto() { return this.parametros.contacto(); }
  readonly anio = new Date().getFullYear();
  get autenticado() { return !!this.storage.obtenerToken(); }
  get nombre() { return this.storage.obtenerNombre() || 'Tu cuenta'; }
  get puedeGestionar() { const rol = this.storage.obtenerRol(); return this.autenticado && rol !== null && ROLES_GESTION.includes(rol); }
  cerrarSesion(): void { this.confirmarSalida.solicitar(()=>{this.storage.limpiarSesion();void this.router.navigate(['/inicio']);}); }
}
