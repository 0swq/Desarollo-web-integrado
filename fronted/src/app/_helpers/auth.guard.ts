import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { StorageService } from '../_services/storage.service';

export const authGuard: CanActivateFn = () => {
  if (!environment.authEnabled) return true;
  return inject(StorageService).obtenerToken() ? true : inject(Router).createUrlTree(['/iniciar-sesion']);
};
