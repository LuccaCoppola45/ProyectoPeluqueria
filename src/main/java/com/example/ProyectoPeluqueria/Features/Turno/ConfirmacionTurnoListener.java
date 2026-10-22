package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Exceptions.TurnoExceptions.TurnoNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class ConfirmacionTurnoListener {

    private static final Logger log = LoggerFactory.getLogger(ConfirmacionTurnoListener.class);

    private final TurnoRepository turnoRepository;
    private final WhatsappService whatsappService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void enviarConfirmacion(TurnoCreadoEvent event) {
        TurnoEntity turno = turnoRepository.findById(event.turnoId())
                .orElseThrow(() -> new TurnoNotFoundException("Turno no encontrado"));
        try {
            if (whatsappService.enviarConfirmacionTurno(turno)) {
                log.info("Meta aceptó el mensaje de confirmación para el turno {}", turno.getIdTurno());
            }
        } catch (IllegalStateException | RestClientException ex) {
            log.error("No se pudo enviar confirmación de WhatsApp para turno {}: {}",
                    turno.getIdTurno(), ex.getMessage());
        }
    }
}
