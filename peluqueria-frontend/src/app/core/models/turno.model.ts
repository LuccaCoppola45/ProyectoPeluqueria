export type EstadoTurno =
  | 'PENDIENTE' | 'CONFIRMADO' | 'CANCELADO' | 'RESERVADO' | 'FINALIZADO';

export interface TurnoRequest {
  fecha: string;       // YYYY-MM-DD
  horaInicio: string;  // HH:mm:ss
  servicioId: number;
  idComercio: number;
  usuarioId?: number;
}

export interface TurnoResponse {
  idTurno: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: EstadoTurno;
  usuarioId: number;
  servicioId: number;
  cancelacionId: number | null;
  idComercio: number;
  nombreUsuario?: string;
  apellidoUsuario?: string;
  nombre?: string;
  apellido?: string;
}
