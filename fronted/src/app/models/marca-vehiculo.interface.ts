// Modelo MarcaVehiculo.java. Las relaciones pueden omitirse en respuestas resumidas.
export interface MarcaVehiculo {
  id: string;
  nombre: string;
  paisOrigen?: string | null;
  imagenUrl?: string | null;
  activo: boolean;
}
