package com.example.ProyectoPeluqueria.Config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

    @RequestMapping({
            "/",
            "/login",
            "/register",
            "/servicios",
            "/turnos",
            "/turnos/**",
            "/admin",
            "/admin/**",
            "/perfil",
            "/perfil/**",
            "/mis-turnos",
            "/mis-turnos/**",
            "/reservar",
            "/reservar/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
