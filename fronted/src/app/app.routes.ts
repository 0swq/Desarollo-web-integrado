import { Routes } from '@angular/router';
import { authGuard } from './_helpers/auth.guard';
import { LoginComponent } from './pages/auth/iniciar-sesion/login';
import { RegisterComponent } from './pages/auth/registro/register';
import { ProductListComponent } from './pages/catalogo/listado-productos/product-list';
import { CartViewComponent } from './pages/carrito/cart-view';
import { InicioComponent } from './pages/inicio/inicio';

export const routes: Routes = [
  { path: 'inicio', component: InicioComponent },
  {
    path: 'nosotros',
    loadComponent: () => import('./pages/nosotros/nosotros').then((m) => m.NosotrosPage),
  },
  { path: 'iniciar-sesion', component: LoginComponent, data: { acceso: true } },
  { path: 'registro', component: RegisterComponent, data: { acceso: true } },
  {
    path: 'recuperar-contrasena',
    data: { acceso: true },
    loadComponent: () =>
      import('./pages/auth/recuperar-contrasena/recuperar-contrasena').then(
        (m) => m.RecuperarContrasenaPage,
      ),
  },
  { path: 'login', redirectTo: 'iniciar-sesion', pathMatch: 'full' },
  { path: 'register', redirectTo: 'registro', pathMatch: 'full' },
  { path: 'catalogo', component: ProductListComponent },
  {
    path: 'catalogo/producto/:id',
    loadComponent: () =>
      import('./pages/catalogo/producto/product-detail').then((m) => m.ProductDetail),
  },
  { path: 'carrito', component: CartViewComponent, canActivate: [authGuard] },
  {
    path: 'administracion',
    data: { administracion: true },
    loadChildren: () =>
      import('./pages/administracion/administracion.routes').then((m) => m.administracionRoutes),
  },
  {
    path: 'pago',
    loadChildren: () => import('./pages/pago/pago.routes').then((m) => m.pagoRoutes),
  },
  {
    path: 'perfil',
    loadChildren: () => import('./pages/perfil/perfil.routes').then((m) => m.perfilRoutes),
  },
  { path: '', redirectTo: 'inicio', pathMatch: 'full' },
  { path: 'error/:codigo', loadComponent: () => import('./pages/error/error').then(m => m.ErrorPage) },
  { path: '**', data: { codigo: '404' }, loadComponent: () => import('./pages/error/error').then(m => m.ErrorPage) },
];

