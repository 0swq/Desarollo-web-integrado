import { Rol } from './rol';

export interface UsuarioResponse {
  id: string;
  correo: string;
  nombre: string;
  apellido: string;
  telefono: string | null;
  direccion: string | null;
  fotoPerfilUrl?: string | null;
  rol: Rol;
  activo: boolean;
  fechaCreacion: string;
  fechaActualizacion: string;
}

export interface LoginRequest {
  correo: string;
  contrasena: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  timestamp?: string;
  data: {
    token: string;
    tokenType: 'Bearer' | string;
    id: string;
    correo: string;
    nombre: string;
    apellido: string;
    fotoPerfilUrl?: string | null;
    rol: Rol;
  };
}

export interface RegisterRequest {
  correo: string;
  contrasena: string;
  nombre: string;
  apellido: string;
  telefono?: string;
  direccion?: string;
  fotoPerfilUrl?: string | null;
  rol?: Rol;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
  timestamp?: string;
}
