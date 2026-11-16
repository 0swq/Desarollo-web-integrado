import type { Proveedor } from './proveedor.interface';

// Modelo Producto.java. Las relaciones pueden omitirse en respuestas resumidas.
export interface Producto {
  id: string;
  sku: string;
  nombre: string;
  descripcion?: string | null;
  precio: number;
  precioPromocional?: number | null;
  moneda?: string | null;
  imagenUrl?: string | null;
  proveedor?: Proveedor | null;
  activo: boolean;
  fechaCreacion?: string | null;
  fechaActualizacion?: string | null;
  proveedorId?: string | null;
}
