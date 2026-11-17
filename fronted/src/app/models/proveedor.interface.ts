                                                                                 
export interface Proveedor {
  id: string;
  ruc: string;
  razonSocial: string;
  contactoNombre?: string | null;
  telefono?: string | null;
  correo?: string | null;
  direccion?: string | null;
  activo: boolean;
  fechaCreacion?: string | null;
  fechaActualizacion?: string | null;
}
