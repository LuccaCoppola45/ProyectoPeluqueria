import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ComercioResponse } from '../models/comercio.model';

@Injectable({ providedIn: 'root' })
export class ComercioService {
  private readonly url = `${environment.apiUrl}/comercios`;

  constructor(private http: HttpClient) {}

  // El backend modela un único comercio (el local); no recibe id.
  obtenerInfo(): Observable<ComercioResponse> {
    return this.http.get<ComercioResponse>(this.url);
  }
}
