import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  readonly menuAbierto = signal(false);

  constructor(public authService: AuthService, private router: Router) {
    // Si el usuario navega (por ejemplo con el teclado), el menú móvil se cierra solo.
    this.router.events
      .pipe(filter((evento) => evento instanceof NavigationEnd))
      .subscribe(() => this.menuAbierto.set(false));
  }

  toggleMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  logout(): void {
    this.authService.logout();
    this.menuAbierto.set(false);
    this.router.navigate(['/']);
  }
}
