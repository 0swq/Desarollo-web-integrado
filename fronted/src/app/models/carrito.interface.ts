import type { Usuario } from './usuario.interface';

export interface Carrito {
  id: string;
  usuario?: Usuario;
  fechaActualizacion?: string | null;
  usuarioId?: string | null;
}
