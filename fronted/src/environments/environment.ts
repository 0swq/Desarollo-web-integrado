import { Environment } from './environment.interface';

export const environment: Environment = {
  production: true,
  apiUrl: 'http://192.168.1.10:8082/api',
  // Activar cuando se termine la prueba del mockup.
  authEnabled: true,
};
