package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RecordatorioTurnoScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioTurnoScheduler.class);

    private final TurnoRepository turnoRepository;
    private final RecordatorioTurnoRepository recordatorioRepository;
    private final WhatsappService whatsappService;

    public RecordatorioTurnoScheduler(
            TurnoRepository turnoRepository,
            RecordatorioTurnoRepository recordatorioRepository,
            WhatsappService whatsappService) {
        this.turnoRepository = turnoRepository;
        this.recordatorioRepository = recordatorioRepository;
        this.whatsappService = whatsappService;
    }

    @Scheduled(fixedDelayString = "${whatsapp.scheduler-delay-ms:300000}")
    @Transactional
    public void enviarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<TurnoEntity> turnos = turnoRepository.findAll().stream()
                .filter(turno -> turno.getEstado() != EstadoTurno.CANCELADO
                        && turno.getEstado() != EstadoTurno.FINALIZADO)
                .toList();

        for (TurnoEntity turno : turnos) {
            LocalDateTime inicio = LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
            enviarSiCorresponde(turno, inicio, ahora, "24H", 23, 25);
            enviarSiCorresponde(turno, inicio, ahora, "12H", 11, 13);
            enviarSiCorresponde(turno, inicio, ahora, "6H", 5, 7);
        }
    }

    private void enviarSiCorresponde(
            TurnoEntity turno,
            LocalDateTime inicio,
            LocalDateTime ahora,
            String tipo,
            int horasMinimas,
            int horasMaximas) {
        long minutos = Duration.between(ahora, inicio).toMinutes();
        if (minutos < horasMinimas * 60L || minutos > horasMaximas * 60L
                || recordatorioRepository.existsByTurno_IdTurnoAndTipo(turno.getIdTurno(), tipo)) {
            return;
        }
        try {
            if (!whatsappService.enviarRecordatorio(turno)) {
                return;
            }
        } catch (IllegalStateException | RestClientException ex) {
            log.error("No se pudo enviar recordatorio {} para turno {}: {}",
                    tipo, turno.getIdTurno(), ex.getMessage());
            return;
        }

        RecordatorioTurnoEntity recordatorio = new RecordatorioTurnoEntity();
        recordatorio.setTurno(turno);
        recordatorio.setTipo(tipo);
        recordatorio.setFechaEnvio(LocalDateTime.now());
        recordatorio.setEstado("ACEPTADO_META");
        recordatorioRepository.save(recordatorio);
        log.info("Meta aceptó el recordatorio {} para turno {}", tipo, turno.getIdTurno());
    }
}
