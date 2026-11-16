// Modelo Parametro.java. Las relaciones pueden omitirse en respuestas resumidas.
export interface Parametro {
  id: string;
  clave: string;
  valor: string;
  descripcion?: string | null;
  fechaActualizacion?: string | null;
}
