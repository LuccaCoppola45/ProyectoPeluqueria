import { Injectable, signal } from '@angular/core';

export type Paleta = 'terracota' | 'negro' | 'noche';

interface OpcionPaleta {
  id: Paleta;
  nombre: string;
  /** Muestra el color principal de cada paleta en el selector. */
  muestra: string;
}

const CLAVE_ALMACEN = 'peluqueria_paleta';

/**
 * Maneja la paleta de colores de la app.
 *
 * La paleta vive en variables CSS (ver styles.css), así que acá solo se
 * cambia una clase en <html>:
 *   - (sin clase)  -> Terracota
 *   - .tema-negro   -> Negro y blanco
 *   - .tema-noche   -> Modo oscuro
 */
@Injectable({ providedIn: 'root' })
export class TemaService {
  readonly paletaActual = signal<Paleta>('terracota');

  readonly opciones: OpcionPaleta[] = [
    { id: 'terracota', nombre: 'Terracota', muestra: '#8a5a44' },
    { id: 'negro', nombre: 'Negro', muestra: '#111111' },
    { id: 'noche', nombre: 'Noche', muestra: '#e6b98a' }
  ];

  constructor() {
    this.aplicar(this.leerGuardada());
  }

  /** Cambia la paleta y la deja anotada para el próximo ingreso. */
  aplicar(paleta: Paleta): void {
    this.paletaActual.set(paleta);

    const html = document.documentElement;
    html.classList.remove('tema-negro', 'tema-noche');
    if (paleta === 'negro') {
      html.classList.add('tema-negro');
    } else if (paleta === 'noche') {
      html.classList.add('tema-noche');
    }

    try {
      localStorage.setItem(CLAVE_ALMACEN, paleta);
    } catch {
      // Si el navegador bloquea el almacenamiento, la paleta igual funciona
      // para esta sesión (simplemente no se recuerda al recargar).
    }
  }

  private leerGuardada(): Paleta {
    try {
      const guardada = localStorage.getItem(CLAVE_ALMACEN) as Paleta | null;
      if (guardada && this.opciones.some((opcion) => opcion.id === guardada)) {
        return guardada;
      }
    } catch {
      // sin almacenamiento: se usa la paleta por defecto
    }
    return 'terracota';
  }
}
