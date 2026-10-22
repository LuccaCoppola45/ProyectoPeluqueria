package com.example.ProyectoPeluqueria.Features.Turno;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "whatsapp")
public record WhatsappProperties(
        boolean enabled,
        String graphUrl,
        String phoneNumberId,
        String accessToken,
        String reminderTemplate,
        String templateLanguageCode) {
}
