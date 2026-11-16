import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { StorageService } from '../_services/storage.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const storage = inject(StorageService);
  const router = inject(Router);
  const token = storage.obtenerToken();
  const isApiRequest = req.url === environment.apiUrl || req.url.startsWith(environment.apiUrl + '/');
  const isAuthRequest = ['/auth/login', '/auth/register'].some(path =>
    req.url.split('?')[0] === environment.apiUrl + path);
  const authReq = token && isApiRequest && !isAuthRequest ? req.clone({ setHeaders: { Authorization: 'Bearer ' + token } }) : req;
  return next(authReq).pipe(catchError(error => {
    if (isApiRequest && !isAuthRequest && error.status === 401 && token === storage.obtenerToken()) {
      storage.limpiarSesion();
      if (environment.authEnabled) void router.navigate(['/iniciar-sesion']);
    }
    return throwError(() => error);
  }));
};
