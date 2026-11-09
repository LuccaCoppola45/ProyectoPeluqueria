package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhooks/whatsapp")
public class WhatsappWebhookController {

    private final String verifyToken;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final RecordatorioTurnoRepository recordatorioRepository;

    public WhatsappWebhookController(
            @Value("${whatsapp.verify-token:}") String verifyToken,
            UsuarioRepository usuarioRepository,
            TurnoRepository turnoRepository,
            RecordatorioTurnoRepository recordatorioRepository) {
        this.verifyToken = verifyToken;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
        this.recordatorioRepository = recordatorioRepository;
    }

    @GetMapping
    public ResponseEntity<String> verificar(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Token de verificación inválido");
    }

    @PostMapping
    public ResponseEntity<Void> recibir(@RequestBody Map<String, Object> payload) {
        String telefono = buscarTexto(payload, "wa_id");
        final String respuesta = buscarRespuesta(payload);
        if (telefono == null || respuesta == null) {
            // Meta también avisa cuando un mensaje se entregó o se leyó: eso no es una respuesta
            return ResponseEntity.ok().build();
        }
        log.info("WhatsApp: llegó la respuesta \"{}\" de {}", respuesta, telefono);

        String telefonoNormalizado = TelefonoWhatsapp.normalizar(telefono);
        UsuarioEntity usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getTelefono() != null
                        && telefonoNormalizado.equals(TelefonoWhatsapp.normalizar(u.getTelefono())))
                .findFirst()
                .orElse(null);
        if (usuario == null) {
            log.warn("WhatsApp: ningún usuario tiene el teléfono {}", telefonoNormalizado);
            return ResponseEntity.ok().build();
        }

        TurnoEntity turno = buscarTurnoRespondido(payload, usuario);
        if (turno == null) {
            log.warn("WhatsApp: el usuario {} no tiene turnos pendientes para responder", usuario.getIdUsuario());
            return ResponseEntity.ok().build();
        }

        String valor = Normalizer.normalize(respuesta, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toUpperCase(Locale.ROOT)
                .trim();
        if (valor.contains("CONFIRM") || valor.matches("^(SI|SÍ)(\\b.*)?$")) {
            turno.setEstado(EstadoTurno.CONFIRMADO);
            turno.setRespuestaCliente("CONFIRMADO");
        } else if (valor.contains("CANCEL") || valor.matches("^NO(\\b.*)?$")) {
            turno.setEstado(EstadoTurno.CANCELADO);
            turno.setRespuestaCliente("CANCELADO");
        } else {
            log.info("WhatsApp: la respuesta \"{}\" no es SÍ ni NO, no se cambia el turno", respuesta);
            return ResponseEntity.ok().build();
        }
        turno.setFechaRespuesta(LocalDateTime.now());
        turnoRepository.save(turno);
        log.info("WhatsApp: turno {} quedó {}", turno.getIdTurno(), turno.getEstado());
        return ResponseEntity.ok().build();
    }

    /**
     * Al apretar un botón, WhatsApp manda en "context.id" el id del mensaje que se contestó:
     * con eso se encuentra el turno exacto. Si no viene (la clienta escribió "no" a mano),
     * se usa el próximo turno pendiente que recibió recordatorio.
     */
    private TurnoEntity buscarTurnoRespondido(Map<String, Object> payload, UsuarioEntity usuario) {
        String mensajeRespondido = buscarTexto(buscarMapa(payload, "context"), "id");
        if (mensajeRespondido != null) {
            TurnoEntity turno = recordatorioRepository.findTurnoByMensajeId(mensajeRespondido)
                    .filter(t -> t.getUsuario() != null
                            && usuario.getIdUsuario().equals(t.getUsuario().getIdUsuario()))
                    .filter(t -> t.getEstado() != EstadoTurno.CANCELADO
                            && t.getEstado() != EstadoTurno.FINALIZADO)
                    .orElse(null);
            if (turno != null) {
                return turno;
            }
            log.info("WhatsApp: el mensaje {} no corresponde a un turno activo, se busca el próximo pendiente",
                    mensajeRespondido);
        }

        List<TurnoEntity> pendientes = turnoRepository.findByUsuario_IdUsuarioAndEstadoOrderByFechaAscHoraInicioAsc(
                        usuario.getIdUsuario(), EstadoTurno.PENDIENTE)
                .stream()
                .filter(t -> !t.getFecha().isBefore(java.time.LocalDate.now()))
                .toList();
        return pendientes.stream()
                .filter(t -> recordatorioRepository.existsByTurno_IdTurno(t.getIdTurno()))
                .findFirst()
                .orElse(pendientes.isEmpty() ? null : pendientes.getFirst());
    }

    private Map<?, ?> buscarMapa(Object value, String key) {
        if (value instanceof Map<?, ?> map) {
            if (map.get(key) instanceof Map<?, ?> encontrado) {
                return encontrado;
            }
            for (Object child : map.values()) {
                Map<?, ?> result = buscarMapa(child, key);
                if (result != null) {
                    return result;
                }
            }
        } else if (value instanceof Iterable<?> iterable) {
            for (Object child : iterable) {
                Map<?, ?> result = buscarMapa(child, key);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private String buscarRespuesta(Object value) {
        if (value instanceof Map<?, ?> map) {
            String text = buscarTexto(map.get("text"), "body");
            if (text != null) {
                return text;
            }
            // Botón de respuesta rápida de una plantilla: {"button": {"text": "SI", "payload": "SI"}}
            Object button = map.get("button");
            if (button instanceof Map<?, ?> boton) {
                Object textoBoton = boton.get("text") != null ? boton.get("text") : boton.get("payload");
                if (textoBoton instanceof String valor) {
                    return valor;
                }
            }
            for (String key : new String[]{"button_reply", "list_reply"}) {
                Object reply = map.get(key);
                String id = buscarTexto(reply, "id");
                if (id != null) {
                    return id;
                }
            }
            for (Object child : map.values()) {
                String result = buscarRespuesta(child);
                if (result != null) {
                    return result;
                }
            }
        } else if (value instanceof Iterable<?> iterable) {
            for (Object child : iterable) {
                String result = buscarRespuesta(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private String buscarTexto(Object value, String key) {
        if (value instanceof Map<?, ?> map) {
            Object direct = map.get(key);
            if (direct instanceof String text) {
                return text;
            }
            for (Object child : map.values()) {
                String result = buscarTexto(child, key);
                if (result != null) {
                    return result;
                }
            }
        } else if (value instanceof Iterable<?> iterable) {
            for (Object child : iterable) {
                String result = buscarTexto(child, key);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
