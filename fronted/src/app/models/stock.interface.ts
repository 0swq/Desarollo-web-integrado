import type { Producto } from './producto.interface';

// Modelo Stock.java. Las relaciones pueden omitirse en respuestas resumidas.
export interface Stock {
  id: string;
  producto?: Producto;
  cantidad: number;
  stockMinimo?: number | null;
  ubicacionAlmacen?: string | null;
  fechaActualizacion?: string | null;
  productoId?: string | null;
}
