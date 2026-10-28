package com.example.ProyectoPeluqueria.Config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class SpaController {

    @GetMapping(value = {"/", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public String index() throws IOException {
        Resource resource = new ClassPathResource("static/index.html");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @GetMapping(value = {
            "/login", "/login/**",
            "/register", "/register/**",
            "/servicios", "/servicios/**",
            "/turnos", "/turnos/**",
            "/mis-turnos", "/mis-turnos/**",
            "/reservar", "/reservar/**",
            "/perfil", "/perfil/**",
            "/admin", "/admin/**"
    }, produces = MediaType.TEXT_HTML_VALUE)
    public String spa() throws IOException {
        Resource resource = new ClassPathResource("static/index.html");
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
