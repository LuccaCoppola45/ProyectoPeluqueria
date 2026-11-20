import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UsuarioResponse } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  constructor(private http: HttpClient) {}

  listar(): Observable<UsuarioResponse[]> {
    return this.http.get<UsuarioResponse[]>(`${environment.apiUrl}/usuarios`);
  }

  /** El usuario logueado cambia su contraseña (tiene que poner la actual). */
  cambiarMiContrasena(contrasenaActual: string, contrasenaNueva: string): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/usuarios/mi-contrasena`, {
      contrasenaActual,
      contrasenaNueva
    });
  }

  /** Solo admin: le pone una contraseña nueva a un cliente (por ejemplo, si se la olvidó). */
  cambiarContrasenaDe(id: number, contrasenaNueva: string): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/usuarios/${id}/contrasena`, { contrasenaNueva });
  }

  /** Borra el usuario junto con sus turnos, cancelaciones y reseñas (solo admin). */
  eliminarConDatos(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/usuarios/${id}`, {
      params: { conDatos: 'true' }
    });
  }
}