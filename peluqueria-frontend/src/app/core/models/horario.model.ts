export type DiaSemana =
  | 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES'
  | 'VIERNES' | 'SABADO' | 'DOMINGO';

export interface HorarioDeAtencionResponse {
  idHorarioAtencion: number;
  comercioId: number;
  diaSemana: DiaSemana;
  horaApertura: string; // HH:mm:ss
  horaCierre: string;
}
