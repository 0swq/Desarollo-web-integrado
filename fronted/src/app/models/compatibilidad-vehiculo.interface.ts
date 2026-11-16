import type { ModeloVehiculo } from './modelo-vehiculo.interface';
import type { Producto } from './producto.interface';

// Modelo CompatibilidadVehiculo.java. Las relaciones pueden omitirse en respuestas resumidas.
export interface CompatibilidadVehiculo {
  id: string;
  producto?: Producto;
  modeloVehiculo?: ModeloVehiculo;
  anioInicio?: number | null;
  anioFin?: number | null;
  motor?: string | null;
  notas?: string | null;
  productoId?: string | null;
  modeloVehiculoId?: string | null;
}
