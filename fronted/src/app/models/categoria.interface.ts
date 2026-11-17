                                                                                 
export interface Categoria {
  id: string;
  nombre: string;
  descripcion?: string | null;
  imagenUrl?: string | null;
  activo: boolean;
  fechaCreacion?: string | null;
  fechaActualizacion?: string | null;
}
