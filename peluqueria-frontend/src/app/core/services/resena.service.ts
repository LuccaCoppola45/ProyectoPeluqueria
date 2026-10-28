import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ResenaResponse } from '../models/resena.model';

@Injectable({ providedIn: 'root' })
export class ResenaService {
  // El endpoint del backend es "/api/resenias" (con "i").
  private readonly url = `${environment.apiUrl}/resenias`;

  constructor(private http: HttpClient) {}

  listar(): Observable<ResenaResponse[]> {
    return this.http.get<ResenaResponse[]>(this.url);
  }

  crear(request: Partial<ResenaResponse>): Observable<ResenaResponse> {
    return this.http.post<ResenaResponse>(this.url, request);
  }
}
