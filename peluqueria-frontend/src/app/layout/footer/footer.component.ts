import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ComercioService } from '../../core/services/comercio.service';
import { AuthService } from '../../core/services/auth.service';
import { ComercioResponse } from '../../core/models/comercio.model';
import { HorarioDeAtencionResponse } from '../../core/models/horario.model';
import { formatearDiaSemana, formatearTelefono } from '../../shared/formato-fecha';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent {
  private readonly comercioService = inject(ComercioService);
  readonly authService = inject(AuthService);

  readonly comercio = signal<ComercioResponse | null>(null);

  constructor() {
    // Los datos del salón son públicos: el pie está disponible para todos,
    // incluso antes de iniciar sesión.
    this.comercioService.obtenerInfo().subscribe({
      next: (datos) => this.comercio.set(datos),
      error: () => this.comercio.set(null)
    });
  }

  /** Agrupa los horarios de atención por día, ordenados de lunes a domingo. */
  readonly ordenDias = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'];

  horariosOrdenados(horarios: HorarioDeAtencionResponse[]): HorarioDeAtencionResponse[] {
    return [...horarios].sort(
      (a, b) =>
        this.ordenDias.indexOf(a.diaSemana) - this.ordenDias.indexOf(b.diaSemana)
    );
  }

  /** Teléfono limpio para el link "tel:", dejando solo dígitos. */
  telefonoLimpio(): string {
    return (this.comercio()?.telefono ?? '').replace(/\D/g, '');
  }

  /** "1122334455" -> "11 2233-4455" */
  telefonoLegible(): string {
    return formatearTelefono(this.comercio()?.telefono);
  }

  /** "MIERCOLES" -> "Miércoles" (el backend no pone tildes). */
  diaLegible(dia: string): string {
    return formatearDiaSemana(dia);
  }

  subir(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
    // Devuelve el foco al inicio del contenido para quien navega con teclado.
    const contenido = document.getElementById('contenido-principal');
    if (contenido) {
      contenido.focus({ preventScroll: true });
    }
  }
}
