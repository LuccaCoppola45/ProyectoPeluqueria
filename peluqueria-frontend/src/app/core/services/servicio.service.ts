import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ServicioRequest, ServicioResponse } from '../models/servicio.model';

@Injectable({ providedIn: 'root' })
export class ServicioService {
  private readonly url = `${environment.apiUrl}/servicios`;

  constructor(private http: HttpClient) {}

  listar(): Observable<ServicioResponse[]> {
    return this.http.get<ServicioResponse[]>(this.url);
  }

  obtenerPorId(id: number): Observable<ServicioResponse> {
    return this.http.get<ServicioResponse>(`${this.url}/${id}`);
  }

  actualizar(id: number, servicio: ServicioRequest): Observable<ServicioResponse> {
    return this.http.put<ServicioResponse>(`${this.url}/${id}`, servicio);
  }
}
