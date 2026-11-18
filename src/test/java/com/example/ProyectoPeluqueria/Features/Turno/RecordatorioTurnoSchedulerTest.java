package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecordatorioTurnoSchedulerTest {

    private final TurnoRepository turnoRepository = mock(TurnoRepository.class);
    private final RecordatorioTurnoRepository recordatorioRepository = mock(RecordatorioTurnoRepository.class);
    private final WhatsappService whatsappService = mock(WhatsappService.class);
    private final RecordatorioTurnoScheduler scheduler = new RecordatorioTurnoScheduler(
            turnoRepository, recordatorioRepository, whatsappService,
            new TransactionTemplate(mock(PlatformTransactionManager.class)));

    /** Simula la tabla recordatorios_turno */
    private final Map<Long, RecordatorioTurnoEntity> tabla = new HashMap<>();

    private TurnoEntity turnoBien;
    private TurnoEntity turnoQueFalla;

    @BeforeEach
    void setUp() {
        LocalDateTime enVentana = LocalDateTime.now().plusHours(24).truncatedTo(ChronoUnit.MINUTES);
        turnoBien = turno(1L, enVentana);
        turnoQueFalla = turno(2L, enVentana);
        when(turnoRepository.findAll()).thenReturn(List.of(turnoBien, turnoQueFalla));
        when(turnoRepository.findById(1L)).thenReturn(Optional.of(turnoBien));
        when(turnoRepository.findById(2L)).thenReturn(Optional.of(turnoQueFalla));
        when(turnoRepository.getReferenceById(1L)).thenReturn(turnoBien);
        when(turnoRepository.getReferenceById(2L)).thenReturn(turnoQueFalla);

        when(recordatorioRepository.existsByTurno_IdTurnoAndTipo(anyLong(), anyString()))
                .thenAnswer(inv -> tabla.values().stream()
                        .anyMatch(r -> r.getTurno().getIdTurno().equals(inv.getArgument(0))
                                && r.getTipo().equals(inv.getArgument(1))));
        when(recordatorioRepository.saveAndFlush(any())).thenAnswer(inv -> {
            RecordatorioTurnoEntity r = inv.getArgument(0);
            r.setId((long) tabla.size() + 100);
            tabla.put(r.getId(), r);
            return r;
        });
        when(recordatorioRepository.findById(anyLong()))
                .thenAnswer(inv -> Optional.ofNullable(tabla.get((Long) inv.getArgument(0))));

        when(whatsappService.enviarRecordatorio(turnoBien)).thenReturn(Optional.of("wamid.1"));
        when(whatsappService.enviarRecordatorio(turnoQueFalla)).thenThrow(new NullPointerException("sin nombre"));
    }

    @Test
    void cadaTurnoRecibeUnSoloRecordatorioAunqueOtroFalle() {
        scheduler.enviarRecordatorios();
        scheduler.enviarRecordatorios();
        scheduler.enviarRecordatorios();

        verify(whatsappService, times(1)).enviarRecordatorio(turnoBien);
        verify(whatsappService, times(1)).enviarRecordatorio(turnoQueFalla);
        assertEquals(2, tabla.size());
        assertEquals(List.of("ACEPTADO_META", "ERROR"), tabla.values().stream()
                .sorted((a, b) -> a.getTurno().getIdTurno().compareTo(b.getTurno().getIdTurno()))
                .map(RecordatorioTurnoEntity::getEstado).toList());
    }

    @Test
    void conWhatsappApagadoNoQuedaRegistroYSeMandaAlPrenderlo() {
        when(turnoRepository.findAll()).thenReturn(List.of(turnoBien));
        when(whatsappService.enviarRecordatorio(turnoBien)).thenReturn(Optional.empty());
        org.mockito.Mockito.doAnswer(inv -> tabla.remove((Long) inv.getArgument(0)))
                .when(recordatorioRepository).deleteById(anyLong());

        scheduler.enviarRecordatorios();
        assertEquals(0, tabla.size());

        when(whatsappService.enviarRecordatorio(turnoBien)).thenReturn(Optional.of("wamid.1"));
        scheduler.enviarRecordatorios();
        scheduler.enviarRecordatorios();

        // Una vez apagado (no cuenta como enviado) y una sola vez ya prendido
        verify(whatsappService, times(2)).enviarRecordatorio(turnoBien);
        assertEquals(1, tabla.size());
    }

    @Test
    void turnoFueraDeVentanaNoSeToca() {
        TurnoEntity lejos = turno(3L, LocalDateTime.now().plusDays(3));
        when(turnoRepository.findAll()).thenReturn(List.of(lejos));

        scheduler.enviarRecordatorios();

        verify(whatsappService, never()).enviarRecordatorio(any());
    }

    @Test
    void elRecordatorioDependeDeConCuantaAnticipacionSeSaco() {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 20, 15, 0);
        assertEquals(Optional.of(RecordatorioTurnoScheduler.PLAN_24H),
                RecordatorioTurnoScheduler.planPara(inicio.minusDays(3), inicio));
        assertEquals(Optional.of(RecordatorioTurnoScheduler.PLAN_24H),
                RecordatorioTurnoScheduler.planPara(inicio.minusHours(30), inicio));
        assertEquals(Optional.of(RecordatorioTurnoScheduler.PLAN_12H),
                RecordatorioTurnoScheduler.planPara(inicio.minusHours(24), inicio));
        assertEquals(Optional.of(RecordatorioTurnoScheduler.PLAN_12H),
                RecordatorioTurnoScheduler.planPara(inicio.minusHours(18), inicio));
        assertEquals(Optional.empty(), RecordatorioTurnoScheduler.planPara(inicio.minusHours(10), inicio));
        // Turnos viejos, sin fecha de reserva guardada
        assertEquals(Optional.of(RecordatorioTurnoScheduler.PLAN_24H),
                RecordatorioTurnoScheduler.planPara(null, inicio));
    }

    @Test
    void siLoSacoUnDiaAntesNoRecibeElDe24hsSinoElDe12hs() {
        LocalDateTime ahora = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        TurnoEntity reservadoRecien = turno(4L, ahora.plusHours(24));
        reservadoRecien.setFechaReserva(ahora.minusMinutes(5));
        when(turnoRepository.findAll()).thenReturn(List.of(reservadoRecien));

        scheduler.enviarRecordatorios();
        verify(whatsappService, never()).enviarRecordatorio(any());

        // 12 hs antes del turno sí le llega
        reservadoRecien.setFecha(ahora.plusHours(12).toLocalDate());
        reservadoRecien.setHoraInicio(ahora.plusHours(12).toLocalTime());
        when(turnoRepository.findById(4L)).thenReturn(Optional.of(reservadoRecien));
        when(turnoRepository.getReferenceById(4L)).thenReturn(reservadoRecien);
        reservadoRecien.setFechaReserva(ahora.minusHours(12));
        when(whatsappService.enviarRecordatorio(reservadoRecien)).thenReturn(Optional.of("wamid.4"));

        scheduler.enviarRecordatorios();
        scheduler.enviarRecordatorios();

        verify(whatsappService, times(1)).enviarRecordatorio(reservadoRecien);
        assertEquals("12H", tabla.values().iterator().next().getTipo());
    }

    private static TurnoEntity turno(Long id, LocalDateTime inicio) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setIdUsuario(id);
        TurnoEntity turno = new TurnoEntity();
        turno.setIdTurno(id);
        turno.setUsuario(usuario);
        turno.setFecha(inicio.toLocalDate());
        turno.setHoraInicio(inicio.toLocalTime());
        turno.setEstado(EstadoTurno.PENDIENTE);
        return turno;
    }
}
