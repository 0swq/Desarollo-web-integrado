import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { StorageService } from '../../_services/storage.service';
const abrirPerfil: CanActivateFn = route => {
  const router = inject(Router);
  if (!inject(StorageService).obtenerToken()) return router.createUrlTree(['/iniciar-sesion']);
  return router.createUrlTree(['/inicio'], { queryParams: { perfil: route.data['seccion'] || 'mi-perfil' } });
};
export const perfilRoutes: Routes = ['', 'mi-perfil', 'mis-pedidos', 'seguridad'].map(path => ({
  path, pathMatch: 'full', canActivate: [abrirPerfil], data: { seccion: path || 'mi-perfil' },
  loadComponent: () => import('./perfil-layout').then(m => m.PerfilLayout),
}));
