import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HorarioDeAtencionResponse } from '../models/horario.model';
import { BloqueoHorarioResponse } from '../models/bloqueo-horario.model';

@Injectable({ providedIn: 'root' })
export class HorarioService {
  private readonly url = `${environment.apiUrl}/horarios`;

  constructor(private http: HttpClient) {}

  actualizar(id: number, horario: Partial<HorarioDeAtencionResponse>): Observable<HorarioDeAtencionResponse> {
    return this.http.put<HorarioDeAtencionResponse>(`${this.url}/${id}`, horario);
  }

  listarBloqueos(
    comercioId: number,
    desde: string,
    hasta: string
  ): Observable<BloqueoHorarioResponse[]> {
    return this.http.get<BloqueoHorarioResponse[]>(`${this.url}/bloqueos`, {
      params: { comercioId, desde, hasta }
    });
  }

  bloquearDia(comercioId: number, fecha: string): Observable<BloqueoHorarioResponse> {
    return this.http.post<BloqueoHorarioResponse>(`${this.url}/bloqueos`, {
      comercioId,
      fecha
    });
  }

  desbloquearDia(comercioId: number, fecha: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/bloqueos/${fecha}`, {
      params: { comercioId }
    });
  }
}