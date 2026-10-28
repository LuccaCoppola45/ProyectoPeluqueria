import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TurnoRequest, TurnoResponse } from '../models/turno.model';

@Injectable({ providedIn: 'root' })
export class TurnoService {
  private readonly url = `${environment.apiUrl}/turnos`;

  constructor(private http: HttpClient) {}

  // Devuelve los horarios de inicio disponibles (LocalTime -> "HH:mm:ss") para
  // una fecha y un servicio dados.
  disponibilidad(fecha: string, servicioId: number): Observable<string[]> {
    const params = new HttpParams()
      .set('fecha', fecha)
      .set('servicioId', servicioId);
    return this.http.get<string[]>(`${this.url}/disponibilidad`, { params });
  }

  crear(request: TurnoRequest): Observable<TurnoResponse> {
    return this.http.post<TurnoResponse>(this.url, request);
  }

  listar(): Observable<TurnoResponse[]> {
    return this.http.get<TurnoResponse[]>(this.url);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
