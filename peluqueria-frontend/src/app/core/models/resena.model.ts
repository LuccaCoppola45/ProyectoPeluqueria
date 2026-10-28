/**
 * Refleja el ReseniaResponse del backend.
 * Ojo: el endpoint es /api/resenias y los campos son
 * "valoracion" y "fechaCreacion" (no "puntuacion" ni "fecha").
 */
export interface ResenaResponse {
  id: number;
  valoracion: number;
  comentario: string;
  fechaCreacion: string;
  comercioId: number;
  usuarioId: number;
  nombreUsuario?: string | null;
  apellidoUsuario?: string | null;
}
