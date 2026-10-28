import { CommonModule } from '@angular/common';
import { Component, input } from '@angular/core';

interface PasoReserva {
  numero: number;
  texto: string;
}

/**
 * Muestra en qué paso de la reserva está la persona.
 * Se usa en las dos pantallas de la reserva para que el recorrido
 * se vea siempre igual:
 *   1. Elegí el servicio
 *   2. Elegí el día
 *   3. Elegí la hora
 *   4. Confirmá
 */
@Component({
  selector: 'app-pasos-reserva',
  standalone: true,
  imports: [CommonModule],
  template: `
    <ol class="pasos" aria-label="Pasos para reservar el turno">
      <li
        *ngFor="let paso of pasos"
        class="paso"
        [class.paso--activo]="paso.numero === actual()"
        [class.paso--hecho]="paso.numero < actual()"
        [attr.aria-current]="paso.numero === actual() ? 'step' : null">
        <span class="paso-numero" aria-hidden="true">{{ paso.numero }}</span>
        <span class="paso-texto">{{ paso.texto }}</span>
      </li>
    </ol>
  `,
  styles: [
    `
      .pasos {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        margin: 0 0 28px;
        padding: 0;
        list-style: none;
      }

      .paso {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 20px 12px 12px;
        border: 2px solid var(--borde);
        border-radius: 999px;
        background: var(--fondo);
        color: var(--texto-medio);
        font-size: 1.05rem;
        font-weight: 700;
      }

      .paso-numero {
        display: grid;
        width: 42px;
        height: 42px;
        place-items: center;
        border-radius: 50%;
        background: var(--borde-2);
        color: var(--texto-suave);
        font-size: 1.2rem;
        font-weight: 800;
      }

      .paso--activo {
        border-color: var(--marca);
        color: var(--marca-texto);
        box-shadow: 0 4px 14px var(--sombra-marca);
      }

      .paso--activo .paso-numero {
        background: var(--marca);
        color: var(--texto-blanco);
      }

      .paso--hecho {
        border-color: #b9c9bc;
        color: #1b6b39;
      }

      .paso--hecho .paso-numero {
        background: #d9eadf;
        color: #16603a;
      }

      @media (max-width: 760px) {
        .pasos {
          gap: 10px;
        }

        .paso {
          flex: 1 1 100%;
        }
      }
    `
  ]
})
export class PasosReservaComponent {
  /** Número del paso en el que está la persona (1 a 4). */
  readonly actual = input.required<number>();

  readonly pasos: PasoReserva[] = [
    { numero: 1, texto: 'Elegí el servicio' },
    { numero: 2, texto: 'Elegí el día' },
    { numero: 3, texto: 'Elegí la hora' },
    { numero: 4, texto: 'Confirmá' }
  ];
}
