import { Routes } from '@angular/router';
import { authGuard } from '../../_helpers/auth.guard';

export const pagoRoutes: Routes = [{
  path: '', loadComponent: () => import('./pago-layout').then(m => m.PagoLayout),
  canActivate: [authGuard], canActivateChild: [authGuard],
  children: [
    { path: '', redirectTo: 'llenar-datos', pathMatch: 'full' },
    { path: 'llenar-datos', loadComponent: () => import('./llenar-datos/llenar-datos').then(m => m.LlenarDatosPage) },
    { path: 'pago', loadComponent: () => import('./pago/pago').then(m => m.PagoPage) },
    { path: 'confirmacion', loadComponent: () => import('./confirmacion/confirmacion').then(m => m.ConfirmacionPage) },
  ],
}];
