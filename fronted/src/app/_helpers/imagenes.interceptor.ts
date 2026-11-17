import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { map } from 'rxjs';
import { environment } from '../../environments/environment';

                                                                                          
function resolverImagenes(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(resolverImagenes);
  if (value === null || typeof value !== 'object') return value;
  return Object.fromEntries(Object.entries(value).map(([key, item]) => {
    if (['imagenUrl', 'fotoPerfilUrl', 'logoUrl'].includes(key) && typeof item === 'string' && item.startsWith('/api/')) {
      return [key, environment.apiUrl.replace(/\/api\/?$/, '') + item];
    }
    return [key, resolverImagenes(item)];
  }));
}
export const imagenesInterceptor: HttpInterceptorFn = (req, next) => next(req).pipe(map(event => {
  if (event instanceof HttpResponse && req.url.startsWith(environment.apiUrl) && req.responseType === 'json') {
    return event.clone({ body: resolverImagenes(event.body) });
  }
  return event;
}));