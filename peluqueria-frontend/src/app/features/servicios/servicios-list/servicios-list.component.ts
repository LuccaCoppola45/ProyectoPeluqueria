import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
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
  imports: [CommonModule, RouterLink, FormsModule],
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

  // Formulario de reseña (solo con sesión iniciada)
  readonly valoracionElegida = signal(0);
  readonly enviandoResena = signal(false);
  readonly resenaMsg = signal<string | null>(null);
  readonly resenaError = signal<string | null>(null);
  readonly opcionesValoracion = [1, 2, 3, 4, 5];
  comentario = '';

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
    private router: Router,
    readonly authService: AuthService
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

  elegirValoracion(valor: number): void {
    this.valoracionElegida.set(valor);
    this.resenaError.set(null);
  }

  enviarResena(): void {
    this.resenaMsg.set(null);
    this.resenaError.set(null);

    const comercio = this.comercio();
    const valoracion = this.valoracionElegida();
    const comentario = this.comentario.trim();

    if (!comercio) {
      this.resenaError.set('No se pudo cargar la información del salón. Recargá la página e intentá de nuevo.');
      return;
    }
    if (valoracion < 1 || valoracion > 5) {
      this.resenaError.set('Elegí de 1 a 5 estrellas.');
      return;
    }
    if (comentario.length < 3) {
      this.resenaError.set('Escribí un comentario corto sobre tu experiencia.');
      return;
    }

    this.enviandoResena.set(true);
    this.resenaService.crear({ comercioId: comercio.idComercio, valoracion, comentario }).subscribe({
      next: (nueva) => {
        this.resenas.update((lista) => [nueva, ...lista]);
        this.valoracionElegida.set(0);
        this.comentario = '';
        this.enviandoResena.set(false);
        this.resenaMsg.set('¡Gracias! Tu reseña ya está publicada.');
      },
      error: (err) => {
        this.enviandoResena.set(false);
        if (err.status === 401 || err.status === 403) {
          // El token venció: se cierra la sesión para que vuelva a entrar
          this.authService.logout();
          this.resenaError.set('Tu sesión venció. Iniciá sesión de nuevo para dejar tu reseña.');
        } else {
          this.resenaError.set('No se pudo publicar la reseña. Intentá de nuevo en un rato.');
        }
      }
    });
  }

  eliminarResena(resena: ResenaResponse): void {
    if (!window.confirm('¿Eliminar esta reseña?')) {
      return;
    }
    this.resenaService.eliminar(resena.id).subscribe({
      next: () => this.resenas.update((lista) => lista.filter((item) => item.id !== resena.id)),
      error: () => this.resenaError.set('No se pudo eliminar la reseña.')
    });
  }

  getEstrellas(puntuacion: number): string {
    const safePuntuacion = Math.max(0, Math.min(5, puntuacion || 0));
    return '★'.repeat(safePuntuacion) + '☆'.repeat(5 - safePuntuacion);
  }
}
