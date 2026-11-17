import type { Carrito } from './carrito.interface';
import type { Producto } from './producto.interface';

                                                                                   
export interface ItemCarrito {
  id: string;
  carrito?: Carrito;
  producto?: Producto;
  cantidad: number;
  precioUnitario: number;
  carritoId?: string | null;
  productoId?: string | null;
}
