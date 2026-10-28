import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, RegisterRequest } from '../models/auth.model';

const TOKEN_KEY = 'peluqueria_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly url = `${environment.apiUrl}/auth`;

  // Señal reactiva para que la navbar sepa si hay sesión iniciada
  readonly logueado = signal<boolean>(this.hayToken());

  constructor(private http: HttpClient) {}

  registrar(request: RegisterRequest): Observable<string> {
    return this.http.post(`${this.url}/register`, request, {
      responseType: 'text'
    });
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.url}/login`, request)
      .pipe(
        tap((res) => {
          if (res.token) {
            localStorage.setItem(TOKEN_KEY, res.token);
            this.logueado.set(true);
          }
        })
      );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.logueado.set(false);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isAdmin(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    try {
      const payload = JSON.parse(this.decodeBase64Url(token.split('.')[1]));
      const roleClaims = [
        payload.authorities,
        payload.roles,
        payload.role,
        payload.auth,
        payload.scope
      ].filter((claim) => claim !== undefined && claim !== null);
      const roleList = roleClaims.flatMap((claim) => Array.isArray(claim) ? claim : String(claim).split(/[ ,]/));
      return roleList.some((role) => {
        const value = typeof role === 'object' ? role.authority : role;
        return String(value).toUpperCase().replace('ROLE_', '') === 'ADMIN';
      });
    } catch {
      return false;
    }
  }

  private hayToken(): boolean {
    return !!localStorage.getItem(TOKEN_KEY);
  }

  private decodeBase64Url(value: string): string {
    const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
    return decodeURIComponent(
      atob(base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '='))
        .split('')
        .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join('')
    );
  }
}
