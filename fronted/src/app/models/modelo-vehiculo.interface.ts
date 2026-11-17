import type { MarcaVehiculo } from './marca-vehiculo.interface';

                                                                                      
export interface ModeloVehiculo {
  id: string;
  nombre: string;
  marca?: MarcaVehiculo;
  tipoVehiculo?: string | null;
  imagenUrl?: string | null;
  activo: boolean;
  marcaId?: string | null;
}
