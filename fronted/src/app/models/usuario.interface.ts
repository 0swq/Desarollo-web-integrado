import type { Rol } from './rol';

                                                                               
                                                                                   
export interface Usuario {
  id: string;
  correo: string;
  contrasena?: string;
  nombre: string;
  apellido?: string | null;
  telefono?: string | null;
  direccion?: string | null;
  fotoPerfilUrl?: string | null;
  rol: Rol;
  activo: boolean;
  fechaCreacion?: string | null;
  fechaActualizacion?: string | null;
}
