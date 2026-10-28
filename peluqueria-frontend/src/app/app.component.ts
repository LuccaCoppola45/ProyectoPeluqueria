import { Component, isDevMode } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './layout/navbar/navbar.component';
import { FooterComponent } from './layout/footer/footer.component';
import { SwitchPaletaComponent } from './shared/switch-paleta/switch-paleta.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, FooterComponent, SwitchPaletaComponent],
  template: `
    <a class="skip-link" href="#contenido-principal">Saltar al contenido principal</a>
    <app-navbar></app-navbar>
    <main id="contenido-principal" tabindex="-1">
      <router-outlet></router-outlet>
    </main>
    <app-footer></app-footer>
    @if (soloDesarrollo) {
      <app-switch-paleta></app-switch-paleta>
    }
  `,
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  /** El selector de paleta solo se muestra en desarrollo. */
  readonly soloDesarrollo = isDevMode();
}
