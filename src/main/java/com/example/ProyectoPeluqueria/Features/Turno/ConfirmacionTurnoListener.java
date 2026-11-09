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
    private final RecordatorioTurnoRepository recordatorioRepository;

    @org.springframework.beans.factory.annotation.Value("${WHATSAPP_CONFIRMAR_AL_RESERVAR:false}")
    private boolean confirmarAlReservar;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviarConfirmacion(TurnoCreadoEvent event) {
        // La plantilla es el recordatorio que pregunta si viene: mandarla al reservar confunde
        // y cuesta un mensaje extra. Se activa solo con WHATSAPP_CONFIRMAR_AL_RESERVAR=true.
        if (!confirmarAlReservar) {
            return;
        }
        TurnoEntity turno = turnoRepository.findById(event.turnoId())
                .orElseThrow(() -> new TurnoNotFoundException("Turno no encontrado"));
        try {
            java.util.Optional<String> enviado = whatsappService.enviarConfirmacionTurno(turno);
            if (enviado.isPresent()) {
                // Se guarda el id del mensaje para que el botón de la respuesta cambie este turno
                RecordatorioTurnoEntity registro = new RecordatorioTurnoEntity();
                registro.setTurno(turno);
                registro.setTipo("RESERVA");
                registro.setFechaEnvio(java.time.LocalDateTime.now());
                registro.setEstado("ACEPTADO_META");
                registro.setMensajeId(enviado.get().isBlank() ? null : enviado.get());
                recordatorioRepository.save(registro);
                log.info("Meta aceptó el mensaje de confirmación para el turno {}", turno.getIdTurno());
            }
        } catch (IllegalStateException | RestClientException ex) {
            log.error("No se pudo enviar confirmación de WhatsApp para turno {}: {}",
                    turno.getIdTurno(), ex.getMessage());
        }
    }
}
