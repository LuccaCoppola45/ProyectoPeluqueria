import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TurnoService } from '../../../core/services/turno.service';
import { ServicioService } from '../../../core/services/servicio.service';
import { ComercioService } from '../../../core/services/comercio.service';
import { TurnoResponse } from '../../../core/models/turno.model';
import { ServicioResponse } from '../../../core/models/servicio.model';
import { ComercioResponse } from '../../../core/models/comercio.model';
import { formatearFechaLarga, aFechaLocal, formatearTelefono } from '../../../shared/formato-fecha';

@Component({
  selector: 'app-mis-turnos',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './mis-turnos.component.html',
  styleUrls: ['./mis-turnos.component.css']
})
export class MisTurnosComponent implements OnInit {
  readonly turnos = signal<TurnoResponse[]>([]);
  readonly servicios = signal<ServicioResponse[]>([]);
  readonly cargando = signal(true);
  readonly errorMsg = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);
  readonly cancelacionPendiente = signal<number | null>(null);
  readonly cancelando = signal(false);
  /** Datos del salón, para mostrar el teléfono real en el mensaje de ayuda. */
  readonly comercio = signal<ComercioResponse | null>(null);

  /** Turnos que todavía no ocurrieron: los que están por venir. */
  readonly proximos = computed(() =>
    this.turnos().filter((turno) => !this.turnoPasado(turno)).sort(this.ordenarPorFecha)
  );

  /** Turnos ya transcurridos, finalizados o cancelados. */
  readonly anteriores = computed(() =>
    this.turnos().filter((turno) => this.turnoPasado(turno)).sort(this.ordenarPorFecha)
  );

  constructor(
    private turnoService: TurnoService,
    private servicioService: ServicioService,
    private comercioService: ComercioService
  ) {}

  ngOnInit(): void {
    this.cargarTurnos();
    this.cargarServicios();
    this.cargarComercio();
  }

  cargarTurnos(): void {
    this.cargando.set(true);
    this.turnoService.listar().subscribe({
      next: (data) => {
        this.turnos.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudieron cargar tus turnos. Revisá tu conexión e intentá de nuevo.');
        this.cargando.set(false);
      }
    });
  }

  private cargarServicios(): void {
    this.servicioService.listar().subscribe({
      next: (data) => this.servicios.set(data),
      error: () => this.servicios.set([])
    });
  }

  private cargarComercio(): void {
    this.comercioService.obtenerInfo().subscribe({
      next: (data) => this.comercio.set(data),
      error: () => this.comercio.set(null)
    });
  }

  /** Teléfono del salón, tal como lo tiene cargado el negocio. */
  telefono(): string {
    return formatearTelefono(this.comercio()?.telefono);
  }

  private readonly ordenarPorFecha = (a: TurnoResponse, b: TurnoResponse): number =>
    a.fecha.localeCompare(b.fecha) || a.horaInicio.localeCompare(b.horaInicio);

  /** Un turno ya pasó si está cancelado, finalizado o su hora de cierre ya pasó. */
  turnoPasado(turno: TurnoResponse): boolean {
    if (turno.estado === 'CANCELADO' || turno.estado === 'FINALIZADO') {
      return true;
    }

    const fin = new Date(`${turno.fecha}T${turno.horaFin}`);
    return !Number.isNaN(fin.getTime()) && fin < new Date();
  }

  /** Solo se pueden cancelar los turnos que todavía no ocurrieron. */
  puedeCancelar(turno: TurnoResponse): boolean {
    return turno.estado !== 'CANCELADO' && turno.estado !== 'FINALIZADO' && !this.turnoPasado(turno);
  }

  estadoTurno(turno: TurnoResponse): string {
    if (turno.estado === 'CANCELADO') {
      return 'Cancelado';
    }

    if (turno.estado === 'FINALIZADO' || this.turnoPasado(turno)) {
      return 'Finalizado';
    }

    if (turno.estado === 'PENDIENTE') {
      return 'Pendiente';
    }

    return turno.estado === 'CONFIRMADO' ? 'Confirmado' : turno.estado;
  }

  /** Clase CSS según el estado, para que se distinga también por color. */
  claseEstado(turno: TurnoResponse): string {
    const estado = this.estadoTurno(turno);
    return `estado-${estado.toLowerCase()}`;
  }

  nombreServicio(turno: TurnoResponse): string {
    const servicio = this.servicios().find((item) => item.idServicio === turno.servicioId);
    return servicio?.nombreServicio ?? 'Servicio del salón';
  }

  fechaLarga(turno: TurnoResponse): string {
    return formatearFechaLarga(turno.fecha);
  }

  dia(turno: TurnoResponse): string {
    return String(aFechaLocal(turno.fecha)?.getDate() ?? '');
  }

  mes(turno: TurnoResponse): string {
    const fecha = aFechaLocal(turno.fecha);
    if (!fecha) {
      return '';
    }
    return new Intl.DateTimeFormat('es-AR', { month: 'short' })
      .format(fecha)
      .replace('.', '');
  }

  anio(turno: TurnoResponse): string {
    return String(aFechaLocal(turno.fecha)?.getFullYear() ?? '');
  }

  hora(turno: TurnoResponse): string {
    return `${turno.horaInicio.substring(0, 5)} a ${turno.horaFin.substring(0, 5)}`;
  }

  pedirCancelacion(turno: TurnoResponse): void {
    this.aviso.set(null);
    this.errorMsg.set(null);
    this.cancelacionPendiente.set(turno.idTurno);
  }

  descartarCancelacion(): void {
    this.cancelacionPendiente.set(null);
  }

  confirmarCancelacion(turno: TurnoResponse): void {
    this.cancelando.set(true);
    this.errorMsg.set(null);
    this.aviso.set(null);

    this.turnoService.eliminar(turno.idTurno).subscribe({
      next: () => {
        this.cancelando.set(false);
        this.cancelacionPendiente.set(null);
        this.aviso.set('Tu turno fue cancelado. Podés reservar uno nuevo cuando quieras.');
        this.cargarTurnos();
      },
      error: () => {
        this.cancelando.set(false);
        this.errorMsg.set('No se pudo cancelar el turno. Intentá de nuevo en un momento.');
      }
    });
  }
}
