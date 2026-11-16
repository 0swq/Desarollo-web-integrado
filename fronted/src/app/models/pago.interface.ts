import type { EstadoPago } from './estado-pago';
import type { Orden } from './orden.interface';

// Modelo Pago.java. Las relaciones pueden omitirse en respuestas resumidas.
export interface Pago {
  id: string;
  orden?: Orden;
  monto: number;
  moneda?: string | null;
  estado: EstadoPago;
  metodoPago?: string | null;
  mercadoPagoPagoId?: string | null;
  mercadoPagoPreferenciaId?: string | null;
  fechaCreacion?: string | null;
  ordenId?: string | null;
}
