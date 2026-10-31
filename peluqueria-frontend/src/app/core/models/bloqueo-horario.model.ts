export interface BloqueoHorarioResponse {
  fecha: string;
  comercioId: number;
  motivo?: string;
}
export interface BloqueoRangoResponse {
  desde: string;
  hasta: string;
  dias: number;
  turnosExistentes: number;
}
