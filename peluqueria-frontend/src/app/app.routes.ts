import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/servicios/servicios-list/servicios-list.component').then(
        (m) => m.ServiciosListComponent
      )
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent)
  },
  {
    path: 'servicios',
    loadComponent: () =>
      import('./features/servicios/servicios-list/servicios-list.component').then(
        (m) => m.ServiciosListComponent
      )
  },
  {
    // Sección de reserva: solo permite elegir el servicio.
    // Es pública para que se pueda ver qué servicios hay sin iniciar sesión.
    path: 'turnos/reservar',
    loadComponent: () =>
      import('./features/turnos/reservar-inicio/reservar-inicio.component').then(
        (m) => m.ReservarInicioComponent
      )
  },
  {
    path: 'turnos/reservar/:servicioId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/turnos/reservar-turno/reservar-turno.component').then(
        (m) => m.ReservarTurnoComponent
      )
  },
  {
    path: 'turnos/mis-turnos',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/turnos/mis-turnos/mis-turnos.component').then(
        (m) => m.MisTurnosComponent
      )
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-panel/admin-panel.component').then(
        (m) => m.AdminPanelComponent
      )
  },
  { path: '**', redirectTo: '' }
];
