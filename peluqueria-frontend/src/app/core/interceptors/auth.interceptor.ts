import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

// Agrega el JWT como header Authorization a cada request hacia la API.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  if (token && !req.url.includes('/auth/')) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(req).pipe(
    catchError((error) => {
      const esEndpointDeBloqueos = req.url.includes('/horarios/bloqueos');
      const esEndpointPublico = req.url.includes('/resenias');

      if (
        error.status === 401 &&
        !req.url.includes('/auth/') &&
        !esEndpointDeBloqueos &&
        !esEndpointPublico
      ) {
        authService.logout();
        router.navigate(['/login'], {
          queryParams: { sesion: 'vencida' }
        });
      }

      return throwError(() => error);
    })
  );
};
