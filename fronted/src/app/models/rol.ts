// Valores definidos en el enum Rol del backend.
export const ROLES = {
  ADMIN: 'ADMIN',
  EMPLEADO: 'EMPLEADO',
  CLIENTE: 'CLIENTE',
} as const;

export type Rol = (typeof ROLES)[keyof typeof ROLES];

// Grupos de acceso según SecurityConfig.java.
export const ROLES_GESTION: readonly Rol[] = [ROLES.ADMIN, ROLES.EMPLEADO];
export const ROLES_USUARIOS: readonly Rol[] = [ROLES.ADMIN];

export function esRol(value: string | null): value is Rol {
  return Object.values(ROLES).some(role => role === value);
}
