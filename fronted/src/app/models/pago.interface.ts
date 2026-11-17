import type { EstadoPago } from './estado-pago';
import type { Orden } from './orden.interface';

                                                                            
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
