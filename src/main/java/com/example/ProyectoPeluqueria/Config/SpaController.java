package com.example.ProyectoPeluqueria.Config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class SpaController {

    @GetMapping(value = {"/", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() {
        try {
            ClassPathResource resource = new ClassPathResource("static/index.html");
            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("index.html not found");
            }
            InputStream is = resource.getInputStream();
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
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
    public ResponseEntity<String> spa() {
        try {
            ClassPathResource resource = new ClassPathResource("static/index.html");
            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("index.html not found");
            }
            InputStream is = resource.getInputStream();
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
