import { CommonModule } from '@angular/common';
import { Component, isDevMode } from '@angular/core';
import { TemaService } from '../../core/services/tema.service';

/**
 * Selector de paleta. SOLO aparece en desarrollo (ng serve): sirve para
 * comparar la paleta cálida con la negra y la oscura antes de decidir.
 * En el build de producción no se renderiza nada.
 */
@Component({
  selector: 'app-switch-paleta',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="switch" role="group" aria-label="Cambiar paleta de colores (solo en desarrollo)">
      <span class="switch-titulo">Paleta</span>
      <button
        *ngFor="let opcion of tema.opciones"
        type="button"
        class="switch-boton"
        [class.switch-boton--activo]="tema.paletaActual() === opcion.id"
        [attr.aria-pressed]="tema.paletaActual() === opcion.id"
        (click)="tema.aplicar(opcion.id)">
        <span class="switch-muestra" [style.background-color]="opcion.muestra" aria-hidden="true"></span>
        {{ opcion.nombre }}
      </button>
    </div>
  `,
  styles: [
    `
      .switch {
        position: fixed;
        right: 12px;
        bottom: 12px;
        z-index: 999;
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 8px;
        max-width: calc(100vw - 24px);
        padding: 10px 12px;
        border: 2px solid var(--borde-fuerte);
        border-radius: 12px;
        background: var(--fondo);
        box-shadow: 0 8px 24px var(--sombra);
      }

      .switch-titulo {
        color: var(--texto-suave);
        font-size: 1rem;
        font-weight: 700;
      }

      .switch-boton {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        min-height: 44px;
        padding: 8px 14px;
        border: 2px solid var(--borde-fuerte);
        border-radius: 999px;
        background: var(--fondo-ticket);
        color: var(--texto);
        font: inherit;
        font-size: 1rem;
        font-weight: 700;
        cursor: pointer;
      }

      .switch-boton--activo {
        border-color: var(--marca);
        background: var(--marca);
        color: var(--texto-blanco);
      }

      .switch-muestra {
        width: 18px;
        height: 18px;
        border: 1px solid var(--borde-fuerte);
        border-radius: 50%;
      }
    `
  ]
})
export class SwitchPaletaComponent {
  readonly soloDesarrollo = isDevMode();

  constructor(public tema: TemaService) {}
}
