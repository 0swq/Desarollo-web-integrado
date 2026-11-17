import type { Orden } from './orden.interface';
import type { Producto } from './producto.interface';

                                                                                 
export interface ItemOrden {
  id: string;
  orden?: Orden;
  producto?: Producto | null;
  nombreProducto: string;
  sku: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  ordenId?: string | null;
  productoId?: string | null;
}
