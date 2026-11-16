// Modelo Categoria.java. Las relaciones pueden omitirse en respuestas resumidas.
export interface Categoria {
  id: string;
  nombre: string;
  descripcion?: string | null;
  imagenUrl?: string | null;
  activo: boolean;
  fechaCreacion?: string | null;
  fechaActualizacion?: string | null;
}
