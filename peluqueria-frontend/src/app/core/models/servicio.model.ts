export interface ServicioResponse {
  idServicio: number;
  nombreServicio: string;
  duracionDelServicio: number; // minutos
  precio: number;
  descripcion?: string;
  idComercio: number;
}

export interface ServicioRequest {
  nombreServicio: string;
  precio: number;
  duracionDelServicio: number; // minutos
  idComercio: number;
}
