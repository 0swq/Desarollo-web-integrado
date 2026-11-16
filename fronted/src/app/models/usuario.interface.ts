import type { Rol } from './rol';

// Modelo Usuario.java. Las relaciones pueden omitirse en respuestas resumidas.
// contrasena solo corresponde a entrada; no debe devolverse al consultar usuarios.
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
