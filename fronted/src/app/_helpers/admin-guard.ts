import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { StorageService } from '../_services/storage.service';
import { ROLES } from '../models/rol';

export const adminGuard: CanActivateFn = () => {
  if (!environment.authEnabled) return true;
  const storage = inject(StorageService);
  const router = inject(Router);
  if (!storage.obtenerToken()) return router.createUrlTree(['/iniciar-sesion']);
  return storage.obtenerRol() === ROLES.ADMIN ? true : router.createUrlTree(['/catalogo']);
};
