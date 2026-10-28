import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ServicioService } from '../../../core/services/servicio.service';
import { ResenaService } from '../../../core/services/resena.service';
import { ComercioService } from '../../../core/services/comercio.service';
import { ServicioResponse } from '../../../core/models/servicio.model';
import { ResenaResponse } from '../../../core/models/resena.model';
import { ComercioResponse } from '../../../core/models/comercio.model';
import { formatearPrecio, formatearDuracion, formatearFechaLarga, formatearDiaSemana } from '../../../shared/formato-fecha';

@Component({
  selector: 'app-servicios-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './servicios-list.component.html',
  styleUrls: ['./servicios-list.component.css']
})
export class ServiciosListComponent implements OnInit {
  readonly servicios = signal<ServicioResponse[]>([]);
  readonly resenas = signal<ResenaResponse[]>([]);
  readonly comercio = signal<ComercioResponse | null>(null);
  readonly cargando = signal(true);
  readonly errorMsg = signal<string | null>(null);
  readonly fotosEnBlancoYNegro = signal(false);

  readonly infoPuntos = [
    {
      titulo: 'Estilo personalizado',
      texto: 'Cada corte y tratamiento se adapta a tu rostro, a tu rutina y a lo que querés transmitir.'
    },
    {
      titulo: 'Atención premium',
      texto: 'Una experiencia cálida, cercana y cuidada, pensada para que te sientas cómodo desde el primer minuto.'
    },
    {
      titulo: 'Peluquero con criterio',
      texto: 'Trabajo profesional con enfoque en cortes modernos, detalles y resultados naturales que duran.'
    }
  ];

  /** Pasos del proceso: le sacan la duda a la persona de qué tiene que hacer. */
  readonly pasosReserva = [
    {
      numero: '1',
      titulo: 'Elegí tu servicio',
      texto: 'Ves todos los servicios con su precio y duración, y tocás el que querés.'
    },
    {
      numero: '2',
      titulo: 'Elegí el día',
      texto: 'En el calendario tocás el día que te quede cómodo.'
    },
    {
      numero: '3',
      titulo: 'Elegí la hora',
      texto: 'Te mostramos las horas libres de ese día y elegís la que te sirva.'
    },
    {
      numero: '4',
      titulo: 'Confirmá',
      texto: 'Revisás los datos y listo. Tu turno queda anotado y lo ves en “Mis turnos”.'
    }
  ];

  constructor(
    private servicioService: ServicioService,
    private resenaService: ResenaService,
    private comercioService: ComercioService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.servicioService.listar().subscribe({
      next: (data) => {
        this.servicios.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.errorMsg.set(
          err.status === 0
            ? 'No se pudo conectar con el servidor. Verificá que el backend esté iniciado para cargar los servicios.'
            : 'No se pudieron cargar los servicios. Intentá de nuevo más tarde.'
        );
        this.cargando.set(false);
      }
    });

    this.resenaService.listar().subscribe({
      next: (data) => this.resenas.set(data),
      error: () => this.resenas.set([])
    });

    this.comercioService.obtenerInfo().subscribe({
      next: (data) => this.comercio.set(data),
      error: () => this.comercio.set(null)
    });
  }

  precioLegible(servicio: ServicioResponse): string {
    return formatearPrecio(servicio.precio);
  }

  duracionLegible(servicio: ServicioResponse): string {
    return formatearDuracion(servicio.duracionDelServicio);
  }

  /** "MIERCOLES" -> "Miércoles" (el backend no pone tildes). */
  diaLegible(dia: string): string {
    return formatearDiaSemana(dia);
  }

  /** Si todavía no hay reseñas no se muestra un promedio inventado. */
  promedio(): string | null {
    const resenas = this.resenas();
    if (resenas.length === 0) {
      return null;
    }

    const suma = resenas.reduce((total, resena) => total + (resena.valoracion || 0), 0);
    return (suma / resenas.length).toFixed(1).replace('.', ',');
  }

  /** "María González" a partir de nombre y apellido sueltos. */
  nombreCliente(resena: ResenaResponse): string {
    const nombre = [resena.nombreUsuario, resena.apellidoUsuario]
      .map((parte) => (parte ?? '').trim())
      .filter((parte) => parte.length > 0)
      .join(' ');
    return this.capitalizar(nombre) || 'Cliente';
  }

  /**
   * Solo se levanta la primera letra del nombre completo: en la base los
   * nombres van en minúscula, pero los apellidos compuestos se respetan
   * ("la civita", no "La Civita").
   */
  private capitalizar(texto: string): string {
    return texto.charAt(0).toUpperCase() + texto.slice(1);
  }

  /** La fecha viene como "2026-10-22T14:30:00"; se muestra como fecha legible. */
  fechaResena(resena: ResenaResponse): string {
    const fecha = resena.fechaCreacion?.split('T')[0];
    return fecha ? formatearFechaLarga(fecha) : '';
  }

  elegirServicio(servicio: ServicioResponse): void {
    this.router.navigate(['/turnos/reservar', servicio.idServicio]);
  }

  alternarTratamientoFotos(): void {
    this.fotosEnBlancoYNegro.update((actual) => !actual);
  }

  getEstrellas(puntuacion: number): string {
    const safePuntuacion = Math.max(0, Math.min(5, puntuacion || 0));
    return '★'.repeat(safePuntuacion) + '☆'.repeat(5 - safePuntuacion);
  }
}
