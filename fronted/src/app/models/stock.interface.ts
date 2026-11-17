import type { Producto } from './producto.interface';

                                                                             
export interface Stock {
  id: string;
  producto?: Producto;
  cantidad: number;
  stockMinimo?: number | null;
  ubicacionAlmacen?: string | null;
  fechaActualizacion?: string | null;
  productoId?: string | null;
}
