import type { Carrito } from './carrito.interface';
import type { Producto } from './producto.interface';

// Modelo ItemCarrito.java. Las relaciones pueden omitirse en respuestas resumidas.
export interface ItemCarrito {
  id: string;
  carrito?: Carrito;
  producto?: Producto;
  cantidad: number;
  precioUnitario: number;
  carritoId?: string | null;
  productoId?: string | null;
}
