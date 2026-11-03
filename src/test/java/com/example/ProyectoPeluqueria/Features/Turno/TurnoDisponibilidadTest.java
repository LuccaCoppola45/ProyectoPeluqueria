package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Comercio.ComercioEntity;
import com.example.ProyectoPeluqueria.Features.Enums.DiaSemana;
import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import com.example.ProyectoPeluqueria.Exceptions.horarioAtencion.HorarioFueraDeAtencionException;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.BloqueoHorarioService;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.dtos.BloqueoHorarioResponse;
import com.example.ProyectoPeluqueria.Features.HorarioDeAtencion.HorarioDeAtencionEntity;
import com.example.ProyectoPeluqueria.Features.Servicio.ServicioEntity;
import com.example.ProyectoPeluqueria.Features.Servicio.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TurnoDisponibilidadTest {

    private static final Long COMERCIO_ID = 1L;
    private static final LocalDate MARTES = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
    private static final LocalDate SABADO = MARTES.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));

    private final TurnoRepository turnoRepository = mock(TurnoRepository.class);
    private final ServicioRepository servicioRepository = mock(ServicioRepository.class);
    private final BloqueoHorarioService bloqueoHorarioService = mock(BloqueoHorarioService.class);
    private final List<TurnoEntity> turnos = new ArrayList<>();

    private TurnoServiceImp turnoService;
    private ServicioEntity corte;
    private ServicioEntity color;
    private ServicioEntity reflejos;
    private ServicioEntity colorAyudante;
    private ServicioEntity basePermanente;

    @BeforeEach
    void setUp() {
        ComercioEntity comercio = new ComercioEntity();
        comercio.setIdComercio(COMERCIO_ID);
        comercio.setHorarios(List.of(horario(comercio, DiaSemana.MARTES), horario(comercio, DiaSemana.SABADO)));

        corte = servicio(1L, "Corte de cabello", 30, comercio);
        color = servicio(2L, "Color", 60, comercio);
        reflejos = servicio(3L, "Reflejos / Balayage", 45, comercio);
        reflejos.setHorariosFijos("09:00,13:00");
        basePermanente = servicio(5L, "Base permanente", 60, comercio);
        basePermanente.setHorariosFijos("09:00,13:00");
        colorAyudante = servicio(4L, "Color con segunda peluquera", 60, comercio);
        colorAyudante.setDeAyudante(true);
        Map<Long, ServicioEntity> servicios = Map.of(1L, corte, 2L, color, 3L, reflejos, 4L, colorAyudante, 5L, basePermanente);

        when(servicioRepository.findById(anyLong()))
                .thenAnswer(inv -> Optional.ofNullable(servicios.get(inv.<Long>getArgument(0))));
        when(servicioRepository.findByComercio_IdComercio(COMERCIO_ID)).thenReturn(List.of(corte, color, reflejos, colorAyudante, basePermanente));
        when(turnoRepository.findByComercio_IdComercioAndFecha(eq(COMERCIO_ID), any()))
                .thenAnswer(inv -> turnos.stream().filter(t -> t.getFecha().equals(inv.getArgument(1))).toList());

        turnoService = new TurnoServiceImp(turnoRepository, null, servicioRepository, null,
                bloqueoHorarioService, null, null);
    }

    @Test
    void losTurnosSonCadaTreintaMinutosYIncluyenLosHorariosDeReflejosSiEstanLibres() {
        assertThat(disponibilidad(MARTES, corte)).containsExactlyElementsOf(horas(
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
                "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"));
    }

    @Test
    void losReflejosYLaBaseSoloSePuedenSacarALasNueveYALasTrece() {
        assertThat(disponibilidad(MARTES, reflejos)).containsExactlyElementsOf(horas("09:00", "13:00"));
        assertThat(disponibilidad(MARTES, basePermanente)).containsExactlyElementsOf(horas("09:00", "13:00"));
    }

    @Test
    void siSeOcupaUnHorarioCompartidoNoLoPuedeSacarElOtroServicio() {
        agendar(MARTES, basePermanente, "09:00");

        assertThat(disponibilidad(MARTES, basePermanente)).containsExactlyElementsOf(horas("13:00"));
        assertThat(disponibilidad(MARTES, reflejos)).containsExactlyElementsOf(horas("13:00"));
    }

    @Test
    void siOtroServicioOcupaElHorarioDeReflejosEseDiaYaNoHayReflejosAEsaHora() {
        agendar(MARTES, corte, "09:30");

        assertThat(disponibilidad(MARTES, reflejos)).containsExactlyElementsOf(horas("13:00"));
        assertThat(disponibilidad(MARTES, basePermanente)).containsExactlyElementsOf(horas("13:00"));
    }

    @Test
    void elColorDelTitularNoSeSuperponeConOtroColor() {
        agendar(MARTES, color, "10:00");

        List<LocalTime> disponibles = disponibilidad(MARTES, color);

        assertThat(disponibles).doesNotContain(hora("09:30"), hora("10:00"), hora("10:30"));
        assertThat(disponibles).contains(hora("09:00"), hora("11:00"), hora("13:00"), hora("16:00"));
    }

    @Test
    void laSegundaPeluqueraAtiendeEntreSemanaHastaLasTrece() {
        agendar(MARTES, corte, "10:00");

        assertThat(disponibilidad(MARTES, colorAyudante)).containsExactlyElementsOf(horas(
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00"));
    }

    @Test
    void laSegundaPeluqueraNoSeSuperponeConSusPropiosTurnos() {
        agendar(MARTES, colorAyudante, "09:30");

        assertThat(disponibilidad(MARTES, colorAyudante)).containsExactlyElementsOf(horas(
                "10:30", "11:00", "11:30", "12:00"));
        // el titular sigue con su agenda
        assertThat(disponibilidad(MARTES, color)).contains(hora("10:00"));
    }

    @Test
    void losDosColoresNoSeQuitanHorariosEntreSi() {
        agendar(MARTES, color, "10:00");
        assertThat(disponibilidad(MARTES, colorAyudante)).contains(hora("10:00"));

        agendar(MARTES, colorAyudante, "11:00");
        assertThat(disponibilidad(MARTES, color)).contains(hora("11:00"));
    }

    @Test
    void unDiaQueNoAbreAvisaQueDiasAtiende() {
        assertThatThrownBy(() -> disponibilidad(MARTES.minusDays(1), corte))
                .isInstanceOf(HorarioFueraDeAtencionException.class)
                .hasMessage("El negocio no atiende los lunes. Podés sacar turno los martes y sábados.");
    }

    @Test
    void unDiaBloqueadoSueltoAvisaQueEseDiaEstaCerrado() {
        bloquear(MARTES);

        assertThatThrownBy(() -> disponibilidad(MARTES, corte))
                .hasMessage("El negocio está cerrado el martes " + diaMes(MARTES) + " y no toma turnos ese día. Elegí otra fecha.");
    }

    @Test
    void variosDiasBloqueadosSeAvisanComoUnPeriodo() {
        // Entre el martes y el sábado el negocio no abre, así que no cortan el período
        bloquear(MARTES, SABADO, SABADO.plusDays(3));

        assertThatThrownBy(() -> disponibilidad(SABADO, corte))
                .hasMessage("El negocio está cerrado del " + diaMes(MARTES) + " al " + diaMes(SABADO.plusDays(3))
                        + " y no toma turnos en esas fechas. Elegí otra fecha.");
    }

    @Test
    void losSabadosLaSegundaPeluqueraAtiendeTodoElDia() {
        agendar(SABADO, corte, "15:00");

        assertThat(disponibilidad(SABADO, colorAyudante)).contains(hora("09:00"), hora("11:00"), hora("16:00"));
        assertThat(disponibilidad(SABADO, corte)).doesNotContain(hora("15:00"));
    }

    private void bloquear(LocalDate... fechas) {
        List<LocalDate> bloqueadas = List.of(fechas);
        when(bloqueoHorarioService.estaBloqueado(eq(COMERCIO_ID), any()))
                .thenAnswer(inv -> bloqueadas.contains(inv.<LocalDate>getArgument(1)));
        when(bloqueoHorarioService.listar(eq(COMERCIO_ID), any(), any())).thenReturn(
                bloqueadas.stream().map(f -> new BloqueoHorarioResponse(null, f, COMERCIO_ID)).toList());
    }

    private static String diaMes(LocalDate fecha) {
        return fecha.format(DateTimeFormatter.ofPattern("d/M"));
    }

    private List<LocalTime> disponibilidad(LocalDate fecha, ServicioEntity servicio) {
        return turnoService.obtenerDisponibilidad(fecha, servicio.getIdServicio());
    }

    private void agendar(LocalDate fecha, ServicioEntity servicio, String inicio) {
        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno((long) turnos.size() + 1);
        turno.setFecha(fecha);
        turno.setHoraInicio(hora(inicio));
        turno.setHoraFin(hora(inicio).plusMinutes(servicio.getDuracionDelServicio()));
        turno.setServicio(servicio);
        turno.setEstado(EstadoTurno.PENDIENTE);
        turnos.add(turno);
    }

    private static HorarioDeAtencionEntity horario(ComercioEntity comercio, DiaSemana dia) {
        HorarioDeAtencionEntity horario = new HorarioDeAtencionEntity();
        horario.setComercio(comercio);
        horario.setDiaSemana(dia);
        horario.setHoraApertura(hora("09:00"));
        horario.setHoraCierre(hora("17:00"));
        return horario;
    }

    private static ServicioEntity servicio(Long id, String nombre, int minutos, ComercioEntity comercio) {
        ServicioEntity servicio = new ServicioEntity();
        servicio.setIdServicio(id);
        servicio.setNombre(nombre);
        servicio.setDuracionDelServicio(minutos);
        servicio.setPrecio(0.0);
        servicio.setComercio(comercio);
        return servicio;
    }

    private static LocalTime hora(String texto) {
        return LocalTime.parse(texto);
    }

    private static List<LocalTime> horas(String... textos) {
        return java.util.Arrays.stream(textos).map(TurnoDisponibilidadTest::hora).toList();
    }
}
