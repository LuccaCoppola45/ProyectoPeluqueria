import { ServicioResponse } from './servicio.model';
import { HorarioDeAtencionResponse } from './horario.model';

export interface ComercioResponse {
  idComercio: number;
  nombre: string;
  direccion: string;
  telefono: string;
  descripcion: string;
  categorias: string[];
  servicios: ServicioResponse[];
  horarios: HorarioDeAtencionResponse[];
  usuarioPropietarioId: number;
}
