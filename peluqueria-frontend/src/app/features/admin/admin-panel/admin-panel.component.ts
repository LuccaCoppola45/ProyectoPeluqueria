import { CommonModule } from '@angular/common';
import { Component, computed, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { ComercioService } from '../../../core/services/comercio.service';
import { HorarioService } from '../../../core/services/horario.service';
import { ServicioService } from '../../../core/services/servicio.service';
import { TurnoService } from '../../../core/services/turno.service';
import { UsuarioService } from '../../../core/services/usuario.service';
import { HorarioDeAtencionResponse } from '../../../core/models/horario.model';
import { BloqueoHorarioResponse } from '../../../core/models/bloqueo-horario.model';
import { ServicioResponse } from '../../../core/models/servicio.model';
import { TurnoResponse } from '../../../core/models/turno.model';
import { UsuarioResponse } from '../../../core/models/usuario.model';
import { DatePickerComponent } from '../../../shared/date-picker/date-picker.component';
import { formatearDiaSemana } from '../../../shared/formato-fecha';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePickerComponent],
  templateUrl: './admin-panel.component.html',
  styleUrls: ['./admin-panel.component.css']
})
export class AdminPanelComponent implements OnInit {
  readonly turnos = signal<TurnoResponse[]>([]);
  readonly servicios = signal<ServicioResponse[]>([]);
  readonly usuarios = signal<UsuarioResponse[]>([]);
  readonly horarios = signal<HorarioDeAtencionResponse[]>([]);
  readonly horariosDisponibles = signal<string[]>([]);
  readonly dias = signal<string[]>([]);
  readonly diasBloqueados = signal<string[]>([]);
  /** Días bloqueados agrupados en períodos seguidos (ej: vacaciones). */
  readonly periodosBloqueados = computed(() => this.agruparPeriodos(this.diasBloqueados()));
  readonly procesandoRango = signal(false);
  rangoDesde = '';
  rangoHasta = '';
  readonly cargando = signal(true);
  readonly buscandoHorarios = signal(false);
  readonly mensaje = signal<string | null>(null);
  readonly errorMsg = signal<string | null>(null);
  readonly mostrarHistorial = signal(false);
  readonly guardandoServicioId = signal<number | null>(null);
  readonly eliminandoUsuarioId = signal<number | null>(null);
  readonly busquedaClientes = signal('');
  readonly busquedaReserva = signal('');
  readonly clientesFiltrados = computed(() =>
    this.usuarios().filter((usuario) => this.coincideBusqueda(usuario, this.busquedaClientes()))
  );
  readonly turnosActivos = computed(() =>
    this.turnos().filter((turno) => turno.estado !== 'CANCELADO' && !this.turnoVencido(turno))
  );
  readonly turnosHistoricos = computed(() =>
    this.turnos().filter((turno) => turno.estado === 'CANCELADO' || this.turnoVencido(turno))
  );
  readonly turnosAgrupados = computed(() => this.agruparTurnos(this.turnosActivos()));
  readonly turnosHistoricosAgrupados = computed(() => this.agruparTurnos(this.turnosHistoricos()));
  readonly fechaMinima = this.formatearFecha(new Date());

  reserva = {
    usuarioId: null as number | null,
    servicioId: null as number | null,
    fecha: '',
    horaInicio: '',
    idComercio: null as number | null
  };

  constructor(
    private turnoService: TurnoService,
    private servicioService: ServicioService,
    private usuarioService: UsuarioService,
    private comercioService: ComercioService,
    private horarioService: HorarioService
  ) {}

  ngOnInit(): void {
    this.dias.set(this.generarProximosDias(21));
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.cargando.set(true);
    this.errorMsg.set(null);

    this.turnoService.listar().subscribe({
      next: (turnos) => this.turnos.set(turnos),
      error: () => this.errorMsg.set('No se pudieron cargar los turnos.')
    });

    this.servicioService.listar().subscribe({
      next: (servicios) => this.servicios.set(servicios),
      error: () => this.errorMsg.set('No se pudieron cargar los servicios.')
    });

    this.usuarioService.listar().subscribe({
      next: (usuarios) => this.usuarios.set(usuarios.filter((usuario) => usuario.rol !== 'ADMIN')),
      error: () => this.errorMsg.set('No se pudieron cargar los usuarios.')
    });

    this.comercioService.obtenerInfo().subscribe({
      next: (comercio) => {
        this.reserva.idComercio = comercio.idComercio;
        this.horarios.set(comercio.horarios ?? []);
        this.cargando.set(false);

        this.cargarBloqueos(comercio.idComercio);
      },
      error: () => {
        this.errorMsg.set('No se pudieron cargar los horarios del comercio.');
        this.cargando.set(false);
      }
    });

  }

  generarProximosDias(cantidad: number): string[] {
    const hoy = new Date();
    return Array.from({ length: cantidad }, (_, indice) => {
      const fecha = new Date(hoy);
      fecha.setDate(hoy.getDate() + indice);
      return this.formatearFecha(fecha);
    });
  }

  formatearFecha(fecha: Date): string {
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${fecha.getFullYear()}-${mes}-${dia}`;
  }

  fechaVisible(fecha: string): string {
    return new Intl.DateTimeFormat('es-AR', {
      weekday: 'short',
      day: 'numeric',
      month: 'short'
    }).format(new Date(`${fecha}T12:00:00`));
  }

  private agruparTurnos(turnos: TurnoResponse[]) {
    const grupos = new Map<string, TurnoResponse[]>();

    [...turnos]
      .sort((a, b) => a.fecha.localeCompare(b.fecha) || a.horaInicio.localeCompare(b.horaInicio))
      .forEach((turno) => {
        const turnosDelDia = grupos.get(turno.fecha) ?? [];
        turnosDelDia.push(turno);
        grupos.set(turno.fecha, turnosDelDia);
      });

    return Array.from(grupos.entries()).map(([fecha, turnosDelDia]) => ({
      fecha,
      etiqueta: this.fechaVisible(fecha),
      turnos: turnosDelDia
    }));
  }

  /** Un turno ya pasó cuando está finalizado o cuando su hora de cierre pasó. */
  turnoVencido(turno: TurnoResponse): boolean {
    if (turno.estado === 'FINALIZADO') {
      return true;
    }

    const horaFin = `${turno.fecha}T${turno.horaFin}`;
    const fechaHoraFin = new Date(horaFin);
    const ahora = new Date();

    return !Number.isNaN(fechaHoraFin.getTime()) && fechaHoraFin < ahora;
  }

  estadoTurno(turno: TurnoResponse): string {
    if (turno.estado === 'CANCELADO') {
      return 'Cancelado';
    }

    if (turno.estado === 'FINALIZADO' || this.turnoVencido(turno)) {
      return 'Finalizado';
    }

    return turno.estado === 'PENDIENTE' ? 'Pendiente' : turno.estado;
  }

  diaBloqueado(fecha: string): boolean {
    return this.diasBloqueados().includes(fecha);
  }

  cambiarBloqueo(fecha: string): void {
    this.limpiarMensajes();
    const bloqueado = this.diaBloqueado(fecha);
    const comercioId = this.reserva.idComercio;

    if (!comercioId) {
      this.errorMsg.set('No se pudo identificar el comercio.');
      return;
    }

    const request: Observable<unknown> = bloqueado
      ? this.horarioService.desbloquearDia(comercioId, fecha)
      : this.horarioService.bloquearDia(comercioId, fecha);

    request.subscribe({
      next: () => {
        this.diasBloqueados.update((dias) => bloqueado
          ? dias.filter((dia) => dia !== fecha)
          : [...dias, fecha]);
        if (this.reserva.fecha === fecha) {
          this.reserva.horaInicio = '';
          this.horariosDisponibles.set([]);
        }
        this.mensaje.set(bloqueado ? 'Día habilitado nuevamente.' : 'Día bloqueado. No se podrán reservar turnos.');
      },
      error: (error) => this.errorMsg.set(
        error.status === 401
          ? 'El backend rechazó la operación de bloqueo. Revisá los permisos de administrador.'
          : 'No se pudo actualizar el bloqueo de ese día.'
      )
    });
  }

  buscarHorarios(): void {
    this.reserva.horaInicio = '';
    this.horariosDisponibles.set([]);
    this.limpiarMensajes();

    if (!this.reserva.fecha || !this.reserva.servicioId) {
      return;
    }

    this.buscandoHorarios.set(true);
    this.turnoService.disponibilidad(this.reserva.fecha, this.reserva.servicioId).subscribe({
      next: (horarios) => {
        this.horariosDisponibles.set(horarios);
        this.buscandoHorarios.set(false);
      },
      error: (error) => {
        this.buscandoHorarios.set(false);
        this.errorMsg.set(
          error.status === 401
            ? 'Tu sesión venció. Volvé a iniciar sesión.'
            : 'No se pudieron consultar los horarios disponibles.'
        );
      }
    });
  }

  seleccionarHorario(horario: string): void {
    this.reserva.horaInicio = horario;
  }

  reservarParaUsuario(): void {
    if (!this.reserva.usuarioId || !this.reserva.servicioId || !this.reserva.fecha ||
        !this.reserva.horaInicio || !this.reserva.idComercio) {
      this.errorMsg.set('Completá usuario, servicio, fecha y horario.');
      return;
    }

    this.turnoService.crear({
      usuarioId: this.reserva.usuarioId,
      servicioId: this.reserva.servicioId,
      fecha: this.reserva.fecha,
      horaInicio: this.reserva.horaInicio,
      idComercio: this.reserva.idComercio
    }).subscribe({
      next: () => {
        this.mensaje.set('Turno agendado correctamente.');
        this.resetSeleccionTurno();
        this.cargarDatos();
      },
      error: () => this.errorMsg.set('No se pudo agendar el turno para ese usuario.')
    });
  }

  private resetSeleccionTurno(): void {
    this.reserva = {
      usuarioId: null,
      servicioId: null,
      fecha: '',
      horaInicio: '',
      idComercio: this.reserva.idComercio
    };
    this.horariosDisponibles.set([]);
    this.buscandoHorarios.set(false);
    this.errorMsg.set(null);
  }

  cancelarTurno(turno: TurnoResponse): void {
    const nombre = this.nombreDelUsuario(turno);
    if (!confirm(`¿Cancelar el turno de ${nombre}?`)) {
      return;
    }

    this.turnoService.eliminar(turno.idTurno).subscribe({
      next: () => {
        this.mensaje.set('Turno cancelado correctamente.');
        this.cargarDatos();
      },
      error: () => this.errorMsg.set('No se pudo cancelar el turno.')
    });
  }

  /** "MIERCOLES" -> "Miércoles" (el backend no pone tildes). */
  diaLegible(dia: string): string {
    return formatearDiaSemana(dia);
  }

  eliminarUsuario(usuario: UsuarioResponse): void {
    this.limpiarMensajes();
    const nombre = `${usuario.nombre} ${usuario.apellido}`.trim();
    const confirmado = window.confirm(
      `¿Eliminar a ${nombre}? También se borran sus turnos, cancelaciones y reseñas. Esta acción no se puede deshacer.`
    );
    if (!confirmado) {
      return;
    }

    this.eliminandoUsuarioId.set(usuario.idUsuario);
    this.usuarioService.eliminarConDatos(usuario.idUsuario).subscribe({
      next: () => {
        this.eliminandoUsuarioId.set(null);
        this.usuarios.update((lista) => lista.filter((item) => item.idUsuario !== usuario.idUsuario));
        if (this.reserva.usuarioId === usuario.idUsuario) {
          this.reserva.usuarioId = null;
        }
        // Sus turnos ya no existen: se recarga la lista de turnos
        this.turnoService.listar().subscribe({
          next: (turnos) => this.turnos.set(turnos),
          error: () => this.errorMsg.set('No se pudieron recargar los turnos.')
        });
        this.mensaje.set(`${nombre} fue eliminado.`);
      },
      error: () => {
        this.eliminandoUsuarioId.set(null);
        this.errorMsg.set(`No se pudo eliminar a ${nombre}.`);
      }
    });
  }

  guardarServicio(servicio: ServicioResponse): void {
    this.limpiarMensajes();
    const precio = Number(servicio.precio);
    const duracion = Number(servicio.duracionDelServicio);
    if (!Number.isFinite(precio) || precio <= 0) {
      this.errorMsg.set(`Ingresá un precio mayor a 0 para ${servicio.nombreServicio}.`);
      return;
    }
    if (!Number.isInteger(duracion) || duracion < 1) {
      this.errorMsg.set(`Ingresá una duración válida (en minutos) para ${servicio.nombreServicio}.`);
      return;
    }
    const idComercio = servicio.idComercio ?? this.reserva.idComercio;
    if (!idComercio) {
      this.errorMsg.set('No se encontró el comercio del servicio.');
      return;
    }

    this.guardandoServicioId.set(servicio.idServicio);
    this.servicioService.actualizar(servicio.idServicio, {
      nombreServicio: servicio.nombreServicio,
      precio,
      duracionDelServicio: duracion,
      idComercio
    }).subscribe({
      next: (actualizado) => {
        this.servicios.update((lista) =>
          lista.map((item) => (item.idServicio === actualizado.idServicio ? actualizado : item))
        );
        this.guardandoServicioId.set(null);
        this.mensaje.set(`${actualizado.nombreServicio} actualizado.`);
      },
      error: () => {
        this.guardandoServicioId.set(null);
        this.errorMsg.set(`No se pudo actualizar ${servicio.nombreServicio}.`);
      }
    });
  }

  guardarHorario(horario: HorarioDeAtencionResponse): void {
    this.horarioService.actualizar(horario.idHorarioAtencion, {
      comercioId: horario.comercioId,
      diaSemana: horario.diaSemana,
      horaApertura: horario.horaApertura,
      horaCierre: horario.horaCierre
    }).subscribe({
      next: () => this.mensaje.set(`Horario del ${this.diaLegible(horario.diaSemana)} actualizado.`),
      error: () => this.errorMsg.set('No se pudo actualizar ese horario.')
    });
  }

  ajustarHorario(
    horario: HorarioDeAtencionResponse,
    campo: 'horaApertura' | 'horaCierre',
    deltaMinutos: number
  ): void {
    const valorActual = horario[campo];
    if (!valorActual) {
      return;
    }

    const [horas, minutos] = valorActual.split(':').map(Number);
    const fecha = new Date();
    fecha.setHours(horas, minutos, 0, 0);
    fecha.setMinutes(fecha.getMinutes() + deltaMinutos);

    const nuevaHora = `${String(fecha.getHours()).padStart(2, '0')}:${String(fecha.getMinutes()).padStart(2, '0')}`;
    horario[campo] = nuevaHora;
  }

  /** Trae los bloqueos de hoy a un año, así se ven también las vacaciones lejanas. */
  cargarBloqueos(comercioId: number): void {
    const hoy = new Date();
    const enUnAnio = new Date(hoy);
    enUnAnio.setFullYear(hoy.getFullYear() + 1);
    this.horarioService
      .listarBloqueos(comercioId, this.formatearFecha(hoy), this.formatearFecha(enUnAnio))
      .subscribe({
        next: (bloqueos) => this.diasBloqueados.set(bloqueos.map((bloqueo) => bloqueo.fecha).sort()),
        error: () => this.errorMsg.set('No se pudieron cargar los días bloqueados.')
      });
  }

  bloquearPeriodo(): void {
    this.limpiarMensajes();
    const comercioId = this.reserva.idComercio;
    const desde = this.rangoDesde;
    const hasta = this.rangoHasta || this.rangoDesde;

    if (!comercioId) {
      this.errorMsg.set('No se pudo identificar el comercio.');
      return;
    }
    if (!desde) {
      this.errorMsg.set('Elegí la fecha desde la que no vas a atender.');
      return;
    }
    if (hasta < desde) {
      this.errorMsg.set('La fecha "hasta" no puede ser anterior a "desde".');
      return;
    }

    this.procesandoRango.set(true);
    this.horarioService.bloquearRango(comercioId, desde, hasta).subscribe({
      next: (resultado) => {
        this.procesandoRango.set(false);
        this.rangoDesde = '';
        this.rangoHasta = '';
        this.cargarBloqueos(comercioId);
        this.limpiarHorarioSiQuedoBloqueado(desde, hasta);
        const dias = resultado.dias === 1 ? '1 día' : `${resultado.dias} días`;
        let texto = `Listo: ${dias} sin atención (${this.fechaVisible(desde)} al ${this.fechaVisible(hasta)}). Ya nadie puede sacar turno esos días.`;
        if (resultado.turnosExistentes > 0) {
          texto += ` Ojo: hay ${resultado.turnosExistentes} turno(s) que ya estaban reservados en esas fechas. ` +
            'Siguen activos: revisalos en la agenda y cancelalos o avisale a esos clientes.';
        }
        this.mensaje.set(texto);
      },
      error: (error) => {
        this.procesandoRango.set(false);
        this.errorMsg.set(error?.error?.message || 'No se pudo bloquear ese período.');
      }
    });
  }

  desbloquearPeriodo(desde: string, hasta: string): void {
    this.limpiarMensajes();
    const comercioId = this.reserva.idComercio;
    if (!comercioId) {
      this.errorMsg.set('No se pudo identificar el comercio.');
      return;
    }
    if (!window.confirm(`¿Volver a atender del ${this.fechaVisible(desde)} al ${this.fechaVisible(hasta)}?`)) {
      return;
    }
    this.procesandoRango.set(true);
    this.horarioService.desbloquearRango(comercioId, desde, hasta).subscribe({
      next: () => {
        this.procesandoRango.set(false);
        this.cargarBloqueos(comercioId);
        this.mensaje.set(`Período habilitado: del ${this.fechaVisible(desde)} al ${this.fechaVisible(hasta)} se pueden volver a sacar turnos.`);
      },
      error: () => {
        this.procesandoRango.set(false);
        this.errorMsg.set('No se pudo habilitar ese período.');
      }
    });
  }

  cantidadDias(desde: string, hasta: string): number {
    const inicio = new Date(`${desde}T12:00:00`).getTime();
    const fin = new Date(`${hasta}T12:00:00`).getTime();
    return Math.round((fin - inicio) / 86400000) + 1;
  }

  private limpiarHorarioSiQuedoBloqueado(desde: string, hasta: string): void {
    if (this.reserva.fecha && this.reserva.fecha >= desde && this.reserva.fecha <= hasta) {
      this.reserva.horaInicio = '';
      this.horariosDisponibles.set([]);
    }
  }

  /** ['2026-12-30','2026-12-31','2027-01-01','2027-01-10'] -> [30/12 al 01/01, 10/01] */
  private agruparPeriodos(fechas: string[]): { desde: string; hasta: string }[] {
    const ordenadas = [...fechas].sort();
    const periodos: { desde: string; hasta: string }[] = [];
    for (const fecha of ordenadas) {
      const ultimo = periodos[periodos.length - 1];
      if (ultimo && this.cantidadDias(ultimo.hasta, fecha) === 2) {
        ultimo.hasta = fecha;
      } else {
        periodos.push({ desde: fecha, hasta: fecha });
      }
    }
    return periodos;
  }

  limpiarMensajes(): void {
    this.mensaje.set(null);
    this.errorMsg.set(null);
  }

  nombreDelUsuario(turno: TurnoResponse): string {
    const nombre = turno.nombreUsuario ?? turno.nombre;
    const apellido = turno.apellidoUsuario ?? turno.apellido;
    const nombreCompleto = [nombre, apellido].filter(Boolean).join(' ').trim();
    if (nombreCompleto) {
      return nombreCompleto;
    }

    const usuario = this.usuarios().find((item) => item.idUsuario === turno.usuarioId);
    return usuario ? `${usuario.nombre} ${usuario.apellido}` : `Usuario ${turno.usuarioId}`;
  }

  emailDelUsuario(turno: TurnoResponse): string | null {
    const usuario = this.usuarios().find((item) => item.idUsuario === turno.usuarioId);
    return usuario?.email ?? null;
  }

  dniDelUsuario(turno: TurnoResponse): string | null {
    const usuario = this.usuarios().find((item) => item.idUsuario === turno.usuarioId);
    return usuario?.dni ?? null;
  }

  nombreDelServicio(turno: TurnoResponse): string {
    const servicio = this.servicios().find((item) => item.idServicio === turno.servicioId);
    return servicio?.nombreServicio ?? 'Servicio no disponible';
  }

  /** Clientes para el turno: los que coinciden con la búsqueda, sin perder el ya elegido. */
  clientesParaReserva(): UsuarioResponse[] {
    const texto = this.busquedaReserva();
    return this.usuarios().filter(
      (usuario) => usuario.idUsuario === this.reserva.usuarioId || this.coincideBusqueda(usuario, texto)
    );
  }

  /** Busca sin importar mayúsculas ni tildes, en nombre, apellido, DNI y email. */
  private coincideBusqueda(usuario: UsuarioResponse, texto: string): boolean {
    const termino = this.normalizar(texto);
    if (!termino) {
      return true;
    }
    const campos = this.normalizar(
      [usuario.nombre, usuario.apellido, usuario.dni ?? '', usuario.email ?? ''].join(' ')
    );
    return termino.split(/\s+/).every((parte) => campos.includes(parte));
  }

  private normalizar(texto: string): string {
    return (texto ?? '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  }

  nombreParaSeleccionar(usuario: UsuarioResponse): string {
    const documento = usuario.dni ? `DNI ${usuario.dni}` : 'DNI no disponible';
    return `${usuario.nombre} ${usuario.apellido} · ${documento}`;
  }
}