export interface ServicioResponse {
  idServicio: number;
  nombreServicio: string;
  duracionDelServicio: number; // minutos
  precio: number;
  /** Precio máximo aproximado; si no hay, se muestra "Desde $X". */
  precioHasta?: number | null;
  /** Únicas horas en que se puede sacar, ej. "09:00,14:00" (vacío = cualquier hora). */
  horariosFijos?: string | null;
  deAyudante?: boolean;
  descripcion?: string;
  idComercio: number;
}

export interface ServicioRequest {
  nombreServicio: string;
  precio: number;
  precioHasta?: number | null;
  horariosFijos?: string | null;
  duracionDelServicio: number; // minutos
  idComercio: number;
}
