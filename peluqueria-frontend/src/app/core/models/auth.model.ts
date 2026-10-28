export interface RegisterRequest {
  nombre: string;
  apellido: string;
  dni: string;
  telefono: string;
  password: string;
}

export interface LoginRequest {
  dni: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  mensaje: string;
  token: string;
}
