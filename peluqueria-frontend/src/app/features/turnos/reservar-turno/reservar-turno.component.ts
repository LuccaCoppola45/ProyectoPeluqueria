import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ServicioService } from '../../../core/services/servicio.service';
import { TurnoService } from '../../../core/services/turno.service';
import { ServicioResponse } from '../../../core/models/servicio.model';
import { DatePickerComponent } from '../../../shared/date-picker/date-picker.component';
import { PasosReservaComponent } from '../../../shared/pasos-reserva/pasos-reserva.component';
import { formatearFechaLarga, formatearPrecio, formatearDuracion } from '../../../shared/formato-fecha';

interface FranjaHoraria {
  nombre: string;
  descripcion: string;
  horarios: string[];
}

@Component({
  selector: 'app-reservar-turno',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DatePickerComponent, PasosReservaComponent],
  templateUrl: './reservar-turno.component.html',
  styleUrls: ['./reservar-turno.component.css']
})
export class ReservarTurnoComponent implements OnInit {
  servicio = signal<ServicioResponse | null>(null);
  idComercio = signal<number | null>(null);

  /** yyyy-MM-dd. Es signal para que el indicador de pasos reaccione al instante. */
  readonly fecha = signal('');
  readonly fechaMinima = this.obtenerFechaActual();
  horarios = signal<string[]>([]);
  horarioSeleccionado = signal<string | null>(null);

  cargandoServicio = signal(true);
  buscandoHorarios = signal(false);
  confirmando = signal(false);
  errorMsg = signal<string | null>(null);
  turnoConfirmado = signal(false);

  /**
   * Los horarios se agrupan por franja del día. Mostrar 30 botones sueltos
   * apila opciones y termina confundiendo a la persona que tiene que elegir uno.
   */
  readonly franjas = computed<FranjaHoraria[]>(() => {
    const horarios = this.horarios();
    if (horarios.length === 0) {
      return [];
    }

    const manana: string[] = [];
    const tarde: string[] = [];
    const noche: string[] = [];

    for (const horario of horarios) {
      const hora = Number(horario.substring(0, 2));
      if (hora < 12) {
        manana.push(horario);
      } else if (hora < 18) {
        tarde.push(horario);
      } else {
        noche.push(horario);
      }
    }

    return [
      { nombre: 'Mañana', descripcion: 'De 00:00 a 11:59 hs', horarios: manana },
      { nombre: 'Tarde', descripcion: 'De 12:00 a 17:59 hs', horarios: tarde },
      { nombre: 'Noche', descripcion: 'Desde 18:00 hs', horarios: noche }
    ].filter((franja) => franja.horarios.length > 0);
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private servicioService: ServicioService,
    private turnoService: TurnoService
  ) {}

  /**
   * Paso en el que está la persona. El servicio ya viene elegido
   * (paso 1), por eso el recorrido arranca en el 2.
   */
  readonly pasoActual = computed<number>(() => {
    if (this.horarioSeleccionado()) {
      return 4;
    }
    if (this.fecha()) {
      return 3;
    }
    return 2;
  });

  private obtenerFechaActual(): string {
    const hoy = new Date();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');
    return `${hoy.getFullYear()}-${mes}-${dia}`;
  }

  fechaVisible(): string {
    if (!this.fecha()) {
      return '';
    }

    return formatearFechaLarga(this.fecha());
  }

  /** "09:30" -> "09:30 hs" (declaración oral natural para la franja horaria) */
  horaLegible(horario: string | null): string {
    if (!horario) {
      return '';
    }
    return `${horario.substring(0, 5)} hs`;
  }

  precioLegible(): string {
    return formatearPrecio(this.servicio()?.precio);
  }

  duracionLegible(): string {
    return formatearDuracion(this.servicio()?.duracionDelServicio);
  }

  ngOnInit(): void {
    const servicioId = Number(this.route.snapshot.paramMap.get('servicioId'));

    if (!servicioId) {
      this.errorMsg.set('No se pudo identificar el servicio elegido.');
      this.cargandoServicio.set(false);
      return;
    }

    this.servicioService.obtenerPorId(servicioId).subscribe({
      next: (data) => {
        this.servicio.set(data);
        this.idComercio.set(data.idComercio);
        this.cargandoServicio.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudo cargar el servicio. Volvé a la lista de servicios e intentá de nuevo.');
        this.cargandoServicio.set(false);
      }
    });
  }

  /** Se elige un día en el calendario y se buscan los horarios libres. */
  elegirFecha(fecha: string): void {
    this.fecha.set(fecha);
    this.buscarHorarios();
  }

  buscarHorarios(): void {
    const servicio = this.servicio();
    const fecha = this.fecha();
    if (!fecha || !servicio) {
      this.horarios.set([]);
      this.horarioSeleccionado.set(null);
      return;
    }

    this.buscandoHorarios.set(true);
    this.horarios.set([]);
    this.horarioSeleccionado.set(null);
    this.errorMsg.set(null);

    this.turnoService.disponibilidad(fecha, servicio.idServicio).subscribe({
      next: (data) => {
        this.horarios.set(data);
        this.buscandoHorarios.set(false);
      },
      error: (err) => {
        this.buscandoHorarios.set(false);
        this.errorMsg.set(
          err.status === 401
            ? 'Tu sesión venció. Volvé a iniciar sesión para consultar los horarios.'
            : 'No se pudo consultar la disponibilidad. Revisá la conexión e intentá de nuevo.'
        );
      }
    });
  }

  seleccionarHorario(horario: string): void {
    this.horarioSeleccionado.set(horario);
    this.errorMsg.set(null);
  }

  confirmarTurno(): void {
    const servicio = this.servicio();
    const comercio = this.idComercio();
    const horario = this.horarioSeleccionado();

    if (!servicio || !comercio || !horario) {
      return;
    }

    this.confirmando.set(true);
    this.errorMsg.set(null);

    this.turnoService
      .crear({
        fecha: this.fecha(),
        horaInicio: horario,
        servicioId: servicio.idServicio,
        idComercio: comercio
      })
      .subscribe({
        next: () => {
          this.confirmando.set(false);
          this.turnoConfirmado.set(true);
          setTimeout(() => this.router.navigate(['/turnos/mis-turnos']), 2000);
        },
        error: () => {
          this.confirmando.set(false);
          this.errorMsg.set('No se pudo reservar el turno. Probá con otro horario.');
        }
      });
  }
}
