package com.example.ProyectoPeluqueria.Features.Turno;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WhatsappService {

    // Meta responde este código cuando el número no está en la lista de destinatarios del número de prueba
    private static final String ERROR_NUMERO_NO_PERMITIDO = "131030";

    // "jueves 5/11" y "10:30" para que el mensaje se lea bien
    private static final java.time.format.DateTimeFormatter FORMATO_FECHA =
            java.time.format.DateTimeFormatter.ofPattern("EEEE d/M", java.util.Locale.of("es", "AR"));
    private static final java.time.format.DateTimeFormatter FORMATO_HORA =
            java.time.format.DateTimeFormatter.ofPattern("HH:mm");

    private final WhatsappProperties properties;
    private final RestClient restClient;

    public WhatsappService(WhatsappProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    /**
     * Devuelve el id del mensaje que dio Meta (vacío si no vino), o Optional.empty() si WhatsApp
     * está apagado y no se mandó nada.
     */
    public Optional<String> enviarRecordatorio(TurnoEntity turno) {
        return enviarPlantillaTurno(turno);
    }

    public Optional<String> enviarConfirmacionTurno(TurnoEntity turno) {
        return enviarPlantillaTurno(turno);
    }

    private Optional<String> enviarPlantillaTurno(TurnoEntity turno) {
        if (!properties.enabled()) {
            return Optional.empty();
        }
        List<String> faltantes = new ArrayList<>();
        if (!StringUtils.hasText(properties.graphUrl())) {
            faltantes.add("WHATSAPP_GRAPH_URL");
        }
        if (!StringUtils.hasText(properties.phoneNumberId())) {
            faltantes.add("WHATSAPP_PHONE_NUMBER_ID");
        }
        if (!StringUtils.hasText(properties.accessToken())) {
            faltantes.add("WHATSAPP_ACCESS_TOKEN");
        }
        if (!StringUtils.hasText(properties.reminderTemplate())) {
            faltantes.add("WHATSAPP_REMINDER_TEMPLATE");
        }
        if (!StringUtils.hasText(properties.templateLanguageCode())) {
            faltantes.add("WHATSAPP_TEMPLATE_LANGUAGE_CODE");
        }
        if (!faltantes.isEmpty()) {
            throw new IllegalStateException(
                    "Faltan variables de configuración de WhatsApp: " + String.join(", ", faltantes));
        }
        if (turno.getUsuario() == null
                || !StringUtils.hasText(turno.getUsuario().getTelefono())) {
            throw new IllegalStateException("El usuario del turno no tiene teléfono");
        }

        String url = properties.graphUrl().replaceAll("/+$", "")
                + "/" + properties.phoneNumberId() + "/messages";
        String telefono = TelefonoWhatsapp.normalizar(turno.getUsuario().getTelefono());
        try {
            return Optional.of(enviar(url, armarMensaje(telefono, turno)));
        } catch (HttpClientErrorException ex) {
            // El número de prueba de Meta guarda los celulares de Argentina sin el 9 (54 + área + número),
            // así que si lo rechaza se reintenta en ese formato. Con un número real se usa el 549.
            String sinNueve = TelefonoWhatsapp.sinNueve(telefono);
            if (sinNueve.equals(telefono) || !ex.getResponseBodyAsString().contains(ERROR_NUMERO_NO_PERMITIDO)) {
                throw ex;
            }
            log.info("Meta rechazó {}, se reintenta sin el 9", telefono);
            return Optional.of(enviar(url, armarMensaje(sinNueve, turno)));
        }
    }

    private Map<String, Object> armarMensaje(String telefono, TurnoEntity turno) {
        return Map.of(
                "messaging_product", "whatsapp",
                "to", telefono,
                "type", "template",
                "template", Map.of(
                        "name", properties.reminderTemplate(),
                        "language", Map.of("code", properties.templateLanguageCode()),
                        "components", new Object[] {
                                Map.of("type", "body", "parameters", new Object[] {
                                        Map.of("type", "text", "text", turno.getUsuario().getNombre()),
                                        Map.of("type", "text", "text", FORMATO_FECHA.format(turno.getFecha())),
                                        Map.of("type", "text", "text", FORMATO_HORA.format(turno.getHoraInicio()))
                                })
                        }));
    }

    private String enviar(String url, Map<String, Object> body) {
        Map<?, ?> respuesta = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + properties.accessToken())
                .body(body)
                .retrieve()
                .body(Map.class);
        return mensajeIdDe(respuesta);
    }

    // Meta responde {"messages": [{"id": "wamid..."}]}
    static String mensajeIdDe(Map<?, ?> respuesta) {
        if (respuesta != null && respuesta.get("messages") instanceof List<?> mensajes
                && !mensajes.isEmpty() && mensajes.getFirst() instanceof Map<?, ?> mensaje
                && mensaje.get("id") instanceof String id) {
            return id;
        }
        return "";
    }
}
