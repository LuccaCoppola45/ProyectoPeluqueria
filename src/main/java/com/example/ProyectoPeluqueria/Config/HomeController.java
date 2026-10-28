package com.example.ProyectoPeluqueria.Config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/index.html"})
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping({
            "/login", "/login/**",
            "/register", "/register/**",
            "/servicios", "/servicios/**",
            "/turnos", "/turnos/**",
            "/mis-turnos", "/mis-turnos/**",
            "/reservar", "/reservar/**",
            "/perfil", "/perfil/**",
            "/admin", "/admin/**"
    })
    public String spa() {
        return "forward:/index.html";
    }
}
