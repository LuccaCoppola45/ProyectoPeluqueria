package com.example.ProyectoPeluqueria.Features.Turno;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class NotificacionesService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public NotificacionesService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void enviarEmailConPlantilla(String destinatario, String asunto, String nombreTemplate, Context context) {
        System.out.println("Hilo actual: " + Thread.currentThread().getName());
        try {
            String htmlContent = templateEngine.process(nombreTemplate, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setFrom("luccacoppola@gmail.com");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}