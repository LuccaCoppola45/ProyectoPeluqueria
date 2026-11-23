export type EstadoTurno =
  | 'PENDIENTE' | 'CONFIRMADO' | 'CANCELADO' | 'RESERVADO' | 'FINALIZADO';

export interface TurnoRequest {
  fecha: string;       // YYYY-MM-DD
  horaInicio: string;  // HH:mm:ss
  servicioId: number;
  idComercio: number;
  usuarioId?: number;
  /** Solo admin: turno para alguien sin cuenta, con el nombre que quiera */
  nombreCliente?: string;
}

export interface TurnoResponse {
  idTurno: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: EstadoTurno;
  /** null en los turnos sin cuenta (los saca el admin con un nombre) */
  usuarioId: number | null;
  servicioId: number;
  cancelacionId: number | null;
  idComercio: number;
  conAyudante?: boolean;
  /** "SIN_RESPUESTA" si se canceló solo por no confirmar el recordatorio */
  respuestaCliente?: string | null;
  nombreUsuario?: string;
  apellidoUsuario?: string;
  nombre?: string;
  apellido?: string;
}
