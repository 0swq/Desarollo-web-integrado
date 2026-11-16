import type { MarcaVehiculo } from './marca-vehiculo.interface';

// Modelo ModeloVehiculo.java. Las relaciones pueden omitirse en respuestas resumidas.
export interface ModeloVehiculo {
  id: string;
  nombre: string;
  marca?: MarcaVehiculo;
  tipoVehiculo?: string | null;
  imagenUrl?: string | null;
  activo: boolean;
  marcaId?: string | null;
}
