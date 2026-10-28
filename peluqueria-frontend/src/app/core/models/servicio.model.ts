export interface ServicioResponse {
  idServicio: number;
  nombreServicio: string;
  duracionDelServicio: number; // minutos
  precio: number;
  descripcion?: string;
  idComercio: number;
}
