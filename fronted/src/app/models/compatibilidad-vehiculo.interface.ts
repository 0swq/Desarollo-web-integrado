import type { ModeloVehiculo } from './modelo-vehiculo.interface';
import type { Producto } from './producto.interface';

                                                                                              
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
