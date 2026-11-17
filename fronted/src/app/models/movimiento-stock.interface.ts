import type { Producto } from './producto.interface';
import type { TipoMovimiento } from './tipo-movimiento';
import type { Usuario } from './usuario.interface';

                                                                                       
export interface MovimientoStock {
  id: string;
  producto?: Producto;
  tipo: TipoMovimiento;
  cantidad: number;
  stockAnterior?: number | null;
  stockNuevo?: number | null;
  motivo?: string | null;
  referencia?: string | null;
  usuario?: Usuario | null;
  fecha?: string | null;
  productoId?: string | null;
  usuarioId?: string | null;
}
