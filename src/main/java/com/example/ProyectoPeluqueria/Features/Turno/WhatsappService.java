package com.example.ProyectoPeluqueria.Features.Turno;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Service
public class WhatsappService {

    private final WhatsappProperties properties;
    private final RestClient restClient;

    public WhatsappService(WhatsappProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    public boolean enviarRecordatorio(TurnoEntity turno) {
        return enviarPlantillaTurno(turno);
    }

    public boolean enviarConfirmacionTurno(TurnoEntity turno) {
        return enviarPlantillaTurno(turno);
    }

    private boolean enviarPlantillaTurno(TurnoEntity turno) {
        if (!properties.enabled()) {
            return false;
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
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", turno.getUsuario().getTelefono(),
                "type", "template",
                "template", Map.of(
                        "name", properties.reminderTemplate(),
                        "language", Map.of("code", properties.templateLanguageCode()),
                        "components", new Object[] {
                                Map.of("type", "body", "parameters", new Object[] {
                                        Map.of("type", "text", "text", turno.getUsuario().getNombre()),
                                        Map.of("type", "text", "text", turno.getFecha().toString()),
                                        Map.of("type", "text", "text", turno.getHoraInicio().toString())
                                })
                        }));

        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + properties.accessToken())
                .body(body)
                .retrieve()
                .toBodilessEntity();
        return true;
    }
}
