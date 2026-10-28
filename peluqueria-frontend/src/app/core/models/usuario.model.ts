export type Rol = 'ADMIN' | 'CLIENTE' | 'COMERCIO';

export interface UsuarioResponse {
  idUsuario: number;
  nombre: string;
  apellido: string;
  dni?: string | null;
  rol: Rol;
  email?: string | null;
  serviciosId: number[];
  turnosIds: number[];
  cancelacionesIds: number[];
}
