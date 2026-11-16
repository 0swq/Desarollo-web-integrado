import { ParametrosService } from './_services/parametros.service';
import { PerfilLayout } from './pages/perfil/perfil-layout';

import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { NavbarComponent } from './_shared/componentes/navbar/navbar';
import { Footer } from './_shared/componentes/footer/footer';
import { ToastLayer } from './_shared/componentes/toast-layer/toast-layer';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterLink, RouterOutlet, NavbarComponent, Footer, ToastLayer, PerfilLayout],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  readonly parametros=inject(ParametrosService);
  private router = inject(Router);
  get esAcceso(): boolean { return this.router.routerState.snapshot.root.firstChild?.data['acceso'] === true; }
  get esAdministracion(): boolean { return this.router.routerState.snapshot.root.firstChild?.data['administracion'] === true; }
  title = 'autopartes-frontend';
}
