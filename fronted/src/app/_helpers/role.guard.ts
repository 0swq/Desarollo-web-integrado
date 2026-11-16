import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { StorageService } from '../_services/storage.service';
import { Rol } from '../models/rol';

export const roleGuard: CanActivateFn = (route) => {
  if (!environment.authEnabled) return true;
  const storage = inject(StorageService);
  const router = inject(Router);
  if (!storage.obtenerToken()) return router.createUrlTree(['/iniciar-sesion']);
  const roles = route.data['roles'] as readonly Rol[] | undefined;
  const role = storage.obtenerRol();
  return !roles?.length || (role !== null && roles.includes(role))
    ? true : router.createUrlTree(['/catalogo']);
};
