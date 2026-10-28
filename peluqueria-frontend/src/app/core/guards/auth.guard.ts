import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Protege las rutas que requieren sesión iniciada (turnos, etc).
 * Si el usuario no tiene sesión, lo manda a iniciar sesión guardando a dónde
 * quería ir, para que al entrar vuelva directamente a esa página
 * (por ejemplo, sigue con la reserva que estaba haciendo).
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.logueado()) {
    return true;
  }

  router.navigate(['/login'], {
    queryParams: { regreso: state.url }
  });
  return false;
};
