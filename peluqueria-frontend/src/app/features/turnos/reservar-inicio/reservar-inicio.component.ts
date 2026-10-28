import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ServicioService } from '../../../core/services/servicio.service';
import { ServicioResponse } from '../../../core/models/servicio.model';
import { formatearPrecio, formatearDuracion } from '../../../shared/formato-fecha';
import { PasosReservaComponent } from '../../../shared/pasos-reserva/pasos-reserva.component';

/**
 * Sección de reserva: SOLO permite elegir el servicio.
 * Es el atajo para llegar al turno sin pasar por la página del salón,
 * que tiene mucha información que no hace falta para reservar.
 */
@Component({
  selector: 'app-reservar-inicio',
  standalone: true,
  imports: [CommonModule, RouterLink, PasosReservaComponent],
  templateUrl: './reservar-inicio.component.html',
  styleUrls: ['./reservar-inicio.component.css']
})
export class ReservarInicioComponent implements OnInit {
  readonly servicios = signal<ServicioResponse[]>([]);
  readonly cargando = signal(true);
  readonly errorMsg = signal<string | null>(null);

  constructor(private servicioService: ServicioService) {}

  ngOnInit(): void {
    this.servicioService.listar().subscribe({
      next: (data) => {
        this.servicios.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorMsg.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor. Verificá que el backend esté iniciado.'
            : 'No se pudieron cargar los servicios. Intentá de nuevo en un momento.'
        );
        this.cargando.set(false);
      }
    });
  }

  precioLegible(servicio: ServicioResponse): string {
    return formatearPrecio(servicio.precio);
  }

  duracionLegible(servicio: ServicioResponse): string {
    return formatearDuracion(servicio.duracionDelServicio);
  }
}
