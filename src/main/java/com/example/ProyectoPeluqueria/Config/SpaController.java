package com.example.ProyectoPeluqueria.Config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {
            "/",
            "/login",
            "/login/**",
            "/register",
            "/register/**",
            "/servicios",
            "/servicios/**",
            "/turnos",
            "/turnos/**",
            "/mis-turnos",
            "/mis-turnos/**",
            "/reservar",
            "/reservar/**",
            "/perfil",
            "/perfil/**",
            "/admin",
            "/admin/**"
    })
    public String redirect() {
        return "forward:/index.html";
    }
}
