package com.example.ProyectoPeluqueria.Features.Turno;

import com.example.ProyectoPeluqueria.Features.Enums.EstadoTurno;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioEntity;
import com.example.ProyectoPeluqueria.Features.Usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/whatsapp")
public class WhatsappWebhookController {

    private final String verifyToken;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;

    public WhatsappWebhookController(
            @Value("${whatsapp.verify-token:}") String verifyToken,
            UsuarioRepository usuarioRepository,
            TurnoRepository turnoRepository) {
        this.verifyToken = verifyToken;
        this.usuarioRepository = usuarioRepository;
        this.turnoRepository = turnoRepository;
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
            return ResponseEntity.ok().build();
        }

        UsuarioEntity usuario = usuarioRepository.findByTelefono(telefono).orElse(null);
        if (usuario == null) {
            return ResponseEntity.ok().build();
        }

        turnoRepository.findByUsuario_IdUsuarioAndEstadoOrderByFechaAscHoraInicioAsc(
                        usuario.getIdUsuario(), EstadoTurno.PENDIENTE)
                .stream()
                .findFirst()
                .ifPresent(turno -> {
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
                        return;
                    }
                    turno.setFechaRespuesta(LocalDateTime.now());
                    turnoRepository.save(turno);
                });
        return ResponseEntity.ok().build();
    }

    private String buscarRespuesta(Object value) {
        if (value instanceof Map<?, ?> map) {
            String text = buscarTexto(map.get("text"), "body");
            if (text != null) {
                return text;
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
