package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Manda un solo recordatorio por turno, que pregunta si viene (SI / NO):
 * - Si lo sacó con 30 hs o más de anticipación: 24 hs antes.
 * - Si lo sacó con entre 18 y 30 hs: 12 hs antes (el de 24 hs le llegaría casi al reservar).
 * - Si lo sacó con menos de 18 hs: no se manda, lo acaba de reservar.
 *
 * El registro del recordatorio se guarda ANTES de llamar a Meta y cada turno va en su propia
 * transacción. Así, si algo falla (otro turno con datos raros, la base, Meta), nunca se vuelve
 * a mandar el mismo recordatorio en la siguiente pasada. Antes todo iba en una sola transacción:
 * un error en cualquier turno deshacía los registros ya guardados y se reenviaban cada pasada.
 */
@Service
public class RecordatorioTurnoScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioTurnoScheduler.class);

    /** Qué recordatorio toca y en qué ventana (horas antes del turno) se manda */
    record Plan(String tipo, int horasMinimas, int horasMaximas) {
    }

    static final Plan PLAN_24H = new Plan("24H", 23, 25);
    static final Plan PLAN_12H = new Plan("12H", 11, 13);
    static final long ANTICIPACION_MINIMA_24H = 30;
    static final long ANTICIPACION_MINIMA_12H = 18;

    private record Pendiente(Long turnoId, String tipo) {
    }

    private final TurnoRepository turnoRepository;
    private final RecordatorioTurnoRepository recordatorioRepository;
    private final WhatsappService whatsappService;
    private final TransactionTemplate transaccion;

    public RecordatorioTurnoScheduler(
            TurnoRepository turnoRepository,
            RecordatorioTurnoRepository recordatorioRepository,
            WhatsappService whatsappService,
            TransactionTemplate transaccion) {
        this.turnoRepository = turnoRepository;
        this.recordatorioRepository = recordatorioRepository;
        this.whatsappService = whatsappService;
        this.transaccion = transaccion;
    }

    @Scheduled(fixedDelayString = "${whatsapp.scheduler-delay-ms:300000}")
    public void enviarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<Pendiente> pendientes = transaccion.execute(estado -> turnosParaRecordar(ahora));
        if (pendientes == null) {
            return;
        }
        for (Pendiente pendiente : pendientes) {
            try {
                enviarRecordatorio(pendiente.turnoId(), pendiente.tipo());
            } catch (RuntimeException ex) {
                // Un turno con problemas no frena a los demás
                log.error("Falló el recordatorio {} del turno {}: {}",
                        pendiente.tipo(), pendiente.turnoId(), ex.getMessage());
            }
        }
    }

    private List<Pendiente> turnosParaRecordar(LocalDateTime ahora) {
        return turnoRepository.findAll().stream()
                .filter(turno -> turno.getEstado() != EstadoTurno.CANCELADO
                        && turno.getEstado() != EstadoTurno.FINALIZADO)
                // Los turnos sin cuenta no tienen teléfono
                .filter(turno -> turno.getUsuario() != null)
                .flatMap(turno -> {
                    LocalDateTime inicio = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
                    return planPara(turno.getFechaReserva(), inicio)
                            .filter(plan -> enVentana(plan, inicio, ahora))
                            .map(plan -> new Pendiente(turno.getIdTurno(), plan.tipo()))
                            .stream();
                })
                .filter(p -> !recordatorioRepository.existsByTurno_IdTurnoAndTipo(p.turnoId(), p.tipo()))
                .toList();
    }

    /** Decide el recordatorio según con cuánta anticipación se sacó el turno */
    static Optional<Plan> planPara(LocalDateTime fechaReserva, LocalDateTime inicio) {
        if (fechaReserva == null) {
            // Turnos de antes de guardar la fecha de reserva
            return Optional.of(PLAN_24H);
        }
        long horas = Duration.between(fechaReserva, inicio).toHours();
        if (horas >= ANTICIPACION_MINIMA_24H) {
            return Optional.of(PLAN_24H);
        }
        if (horas >= ANTICIPACION_MINIMA_12H) {
            return Optional.of(PLAN_12H);
        }
        return Optional.empty();
    }

    private static boolean enVentana(Plan plan, LocalDateTime inicio, LocalDateTime ahora) {
        long minutos = Duration.between(ahora, inicio).toMinutes();
        return minutos >= plan.horasMinimas() * 60L && minutos <= plan.horasMaximas() * 60L;
    }

    private void enviarRecordatorio(Long turnoId, String tipo) {
        Long recordatorioId = reservar(turnoId, tipo);
        if (recordatorioId == null) {
            return;
        }

        Optional<String> enviado;
        try {
            enviado = transaccion.execute(estado ->
                    whatsappService.enviarRecordatorio(turnoRepository.findById(turnoId).orElseThrow()));
        } catch (RuntimeException ex) {
            // Queda marcado como ERROR: no se reintenta, para no mandarle varios mensajes a la clienta
            marcar(recordatorioId, "ERROR", null);
            throw ex;
        }

        if (enviado == null || enviado.isEmpty()) {
            // WhatsApp está apagado: se borra la reserva para mandarlo cuando se prenda
            transaccion.executeWithoutResult(estado -> recordatorioRepository.deleteById(recordatorioId));
            return;
        }
        String mensajeId = enviado.get().isBlank() ? null : enviado.get();
        marcar(recordatorioId, "ACEPTADO_META", mensajeId);
        log.info("Meta aceptó el recordatorio {} para turno {}", tipo, turnoId);
    }

    /**
     * Guarda el recordatorio antes de mandarlo. Si ya existe (por ejemplo, dos instancias a la vez
     * durante un deploy), la restricción única lo frena y se devuelve null.
     */
    private Long reservar(Long turnoId, String tipo) {
        try {
            return transaccion.execute(estado -> {
                RecordatorioTurnoEntity recordatorio = new RecordatorioTurnoEntity();
                recordatorio.setTurno(turnoRepository.getReferenceById(turnoId));
                recordatorio.setTipo(tipo);
                recordatorio.setFechaEnvio(LocalDateTime.now());
                recordatorio.setEstado("ENVIANDO");
                return recordatorioRepository.saveAndFlush(recordatorio).getId();
            });
        } catch (DataIntegrityViolationException ex) {
            log.info("El recordatorio {} del turno {} ya estaba registrado", tipo, turnoId);
            return null;
        }
    }

    private void marcar(Long recordatorioId, String estadoEnvio, String mensajeId) {
        try {
            transaccion.executeWithoutResult(estado -> recordatorioRepository.findById(recordatorioId)
                    .ifPresent(recordatorio -> {
                        recordatorio.setEstado(estadoEnvio);
                        recordatorio.setMensajeId(mensajeId);
                        recordatorio.setFechaEnvio(LocalDateTime.now());
                        recordatorioRepository.save(recordatorio);
                    }));
        } catch (RuntimeException ex) {
            // El registro ya existe como ENVIANDO, así que igual no se reenvía
            log.error("No se pudo actualizar el recordatorio {}: {}", recordatorioId, ex.getMessage());
        }
    }
}
