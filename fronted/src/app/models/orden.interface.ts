import type { EstadoOrden } from './estado-orden';
import type { Usuario } from './usuario.interface';

                                                                             
export interface Orden {
  id: string;
  numeroOrden: string;
  usuario?: Usuario;
  estado: EstadoOrden;
  subtotal?: number | null;
  igv?: number | null;
  total?: number | null;
  moneda?: string | null;
  direccionEntrega?: string | null;
  telefonoContacto?: string | null;
  notas?: string | null;
  fechaCreacion?: string | null;
  fechaActualizacion?: string | null;
  usuarioId?: string | null;
}

                                                                               
export interface ItemCarritoResponse {
  id: string;
  productoId: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface CarritoResponse {
  id: string;
  usuarioId: string;
  items: ItemCarritoResponse[];
  totalItems: number;
  total: number;
  moneda: string;
}

export interface OrdenRequest {
  direccionEntrega: string;
  telefonoContacto: string;
  notas?: string;
  metodoPago?: string;
}
