import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ComercioService } from '../../../core/services/comercio.service';
import { ComercioResponse } from '../../../core/models/comercio.model';
import { formatearTelefono } from '../../../shared/formato-fecha';

/**
 * Política de privacidad pública. Meta la pide para publicar la app de WhatsApp,
 * y también sirve como instrucciones para pedir que se borren los datos.
 */
@Component({
  selector: 'app-privacidad',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './privacidad.component.html',
  styleUrls: ['./privacidad.component.css']
})
export class PrivacidadComponent {
  private readonly comercioService = inject(ComercioService);

  readonly comercio = signal<ComercioResponse | null>(null);
  readonly ultimaActualizacion = '16 de noviembre de 2026';

  constructor() {
    this.comercioService.obtenerInfo().subscribe({
      next: (datos) => this.comercio.set(datos),
      error: () => this.comercio.set(null)
    });
  }

  telefonoLimpio(): string {
    return (this.comercio()?.telefono ?? '').replace(/\D/g, '');
  }

  telefonoLegible(): string {
    return formatearTelefono(this.comercio()?.telefono);
  }
}
