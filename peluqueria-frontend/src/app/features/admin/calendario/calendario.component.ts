import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ServicioService } from '../../../core/services/servicio.service';
import { TurnoService } from '../../../core/services/turno.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { ServicioResponse } from '../../../core/models/servicio.model';
import { TurnoResponse } from '../../../core/models/turno.model';
import { UsuarioResponse } from '../../../core/models/usuario.model';
import { formatearFechaLarga } from '../../../shared/formato-fecha';

type MomentoTurno = 'terminado' | 'en-curso' | 'siguiente' | 'pendiente';

interface TurnoDelDia {
  turno: TurnoResponse;
  momento: MomentoTurno;
}

/** Cada cuánto se vuelven a pedir los turnos, para ver los que se sacan mientras la página está abierta. */
const RECARGA_TURNOS_MS = 2 * 60 * 1000;
/** Cada cuánto se mueve el "ahora" (turno en curso / siguiente). */
const TICK_RELOJ_MS = 30 * 1000;

/**
 * Agenda del día para tener abierta mientras se atiende: quién está ahora, a quién le toca
 * después y todos los turnos del día en orden.
 */
@Component({
  selector: 'app-calendario',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './calendario.component.html',
  styleUrls: ['./calendario.component.css']
})
export class CalendarioComponent implements OnInit, OnDestroy {
  readonly fecha = signal(this.formatearFecha(new Date()));
  readonly ahora = signal(new Date());
  readonly turnos = signal<TurnoResponse[]>([]);
  readonly servicios = signal<ServicioResponse[]>([]);
  readonly usuarios = signal<UsuarioResponse[]>([]);
  readonly cargando = signal(true);
  readonly errorMsg = signal<string | null>(null);
  readonly ultimaActualizacion = signal<Date | null>(null);

  readonly esHoy = computed(() => this.fecha() === this.formatearFecha(this.ahora()));
  readonly fechaVisible = computed(() => formatearFechaLarga(this.fecha()));

  readonly turnosDelDia = computed<TurnoDelDia[]>(() => {
    const delDia = this.turnos()
      .filter((turno) => turno.fecha === this.fecha() && turno.estado !== 'CANCELADO')
      .sort((a, b) => a.horaInicio.localeCompare(b.horaInicio));
    const momentos = this.calcularMomentos(delDia);
    return delDia.map((turno, indice) => ({ turno, momento: momentos[indice] }));
  });

  /** Tarjetas de arriba: primero las de él y después las de ella, cada una con "ahora" y "sigue". */
  readonly destacados = computed(() =>
    this.turnosDelDia()
      .filter((item) => item.momento === 'en-curso' || item.momento === 'siguiente')
      .sort((a, b) => Number(!!a.turno.conAyudante) - Number(!!b.turno.conAyudante))
  );
  readonly quedan = computed(() => this.turnosDelDia().filter((item) => item.momento !== 'terminado').length);

  private intervaloReloj?: ReturnType<typeof setInterval>;
  private intervaloRecarga?: ReturnType<typeof setInterval>;

  constructor(
    private turnoService: TurnoService,
    private servicioService: ServicioService,
    private usuarioService: UsuarioService
  ) {}

  ngOnInit(): void {
    this.cargar();
    this.intervaloReloj = setInterval(() => this.ahora.set(new Date()), TICK_RELOJ_MS);
    this.intervaloRecarga = setInterval(() => this.recargarTurnos(), RECARGA_TURNOS_MS);
  }

  ngOnDestroy(): void {
    clearInterval(this.intervaloReloj);
    clearInterval(this.intervaloRecarga);
  }

  cargar(): void {
    this.cargando.set(true);
    this.errorMsg.set(null);
    forkJoin({
      turnos: this.turnoService.listar(),
      servicios: this.servicioService.listar(),
      usuarios: this.usuarioService.listar()
    }).subscribe({
      next: ({ turnos, servicios, usuarios }) => {
        this.turnos.set(turnos);
        this.servicios.set(servicios);
        this.usuarios.set(usuarios);
        this.ultimaActualizacion.set(new Date());
        this.cargando.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudieron cargar los turnos. Revisá la conexión y tocá "Actualizar".');
        this.cargando.set(false);
      }
    });
  }

  /** Recarga silenciosa: si falla, se dejan los turnos que ya había en pantalla. */
  recargarTurnos(): void {
    this.turnoService.listar().subscribe({
      next: (turnos) => {
        this.turnos.set(turnos);
        this.ultimaActualizacion.set(new Date());
        this.errorMsg.set(null);
      },
      error: () => this.errorMsg.set('No se pudo actualizar. Se muestran los últimos turnos cargados.')
    });
  }

  irAHoy(): void {
    this.ahora.set(new Date());
    this.fecha.set(this.formatearFecha(this.ahora()));
  }

  moverDia(dias: number): void {
    const fecha = new Date(`${this.fecha()}T12:00:00`);
    fecha.setDate(fecha.getDate() + dias);
    this.fecha.set(this.formatearFecha(fecha));
  }

  elegirFecha(fecha: string): void {
    if (fecha) {
      this.fecha.set(fecha);
    }
  }

  hora(horario: string): string {
    return horario.substring(0, 5);
  }

  nombreCliente(turno: TurnoResponse): string {
    const nombre = [turno.nombreUsuario ?? turno.nombre, turno.apellidoUsuario ?? turno.apellido]
      .filter(Boolean)
      .join(' ')
      .trim();
    if (nombre) {
      return this.capitalizar(nombre);
    }
    const usuario = this.usuarios().find((item) => item.idUsuario === turno.usuarioId);
    return usuario ? this.capitalizar(`${usuario.nombre} ${usuario.apellido}`) : 'Cliente';
  }

  /** En la base los nombres van en minúscula: "lucía fernández" -> "Lucía Fernández". */
  private capitalizar(texto: string): string {
    return texto.replace(/(^|\s)(\p{L})/gu, (_, espacio: string, letra: string) => espacio + letra.toUpperCase());
  }

  dniCliente(turno: TurnoResponse): string | null {
    return this.usuarios().find((item) => item.idUsuario === turno.usuarioId)?.dni ?? null;
  }

  nombreServicio(turno: TurnoResponse): string {
    return this.servicios().find((item) => item.idServicio === turno.servicioId)?.nombreServicio ?? 'Servicio';
  }

  /** "en 25 min" / "en 1 h 10 min" hasta que empiece el turno. */
  faltaPara(turno: TurnoResponse): string {
    const minutos = Math.max(0, Math.round((this.aFecha(turno.horaInicio).getTime() - this.ahora().getTime()) / 60000));
    if (minutos === 0) {
      return 'ya';
    }
    const horas = Math.floor(minutos / 60);
    const resto = minutos % 60;
    return horas === 0 ? `en ${resto} min` : `en ${horas} h${resto ? ` ${resto} min` : ''}`;
  }

  etiquetaMomento(momento: MomentoTurno): string {
    switch (momento) {
      case 'terminado':
        return 'Terminado';
      case 'en-curso':
        return 'Ahora';
      case 'siguiente':
        return 'Sigue';
      case 'pendiente':
        return '';
    }
  }

  /**
   * Cada peluquero tiene su agenda, así que "en curso" y "sigue" se calculan por separado:
   * puede estar él con un corte y ella con un color al mismo tiempo.
   */
  private calcularMomentos(turnos: TurnoResponse[]): MomentoTurno[] {
    const momentos: MomentoTurno[] = turnos.map(() => 'pendiente');
    const hoy = this.formatearFecha(this.ahora());
    if (this.fecha() < hoy) {
      return momentos.map(() => 'terminado');
    }
    if (this.fecha() > hoy) {
      return momentos;
    }

    const ahora = this.ahora().getTime();
    const yaTieneSiguiente = new Set<boolean>();
    turnos.forEach((turno, indice) => {
      const inicio = this.aFecha(turno.horaInicio).getTime();
      const fin = this.aFecha(turno.horaFin).getTime();
      const deAyudante = !!turno.conAyudante;
      if (fin <= ahora) {
        momentos[indice] = 'terminado';
      } else if (inicio <= ahora) {
        momentos[indice] = 'en-curso';
      } else if (!yaTieneSiguiente.has(deAyudante)) {
        momentos[indice] = 'siguiente';
        yaTieneSiguiente.add(deAyudante);
      }
    });
    return momentos;
  }

  private aFecha(horario: string): Date {
    return new Date(`${this.fecha()}T${horario.substring(0, 5)}:00`);
  }

  private formatearFecha(fecha: Date): string {
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${fecha.getFullYear()}-${mes}-${dia}`;
  }
}
