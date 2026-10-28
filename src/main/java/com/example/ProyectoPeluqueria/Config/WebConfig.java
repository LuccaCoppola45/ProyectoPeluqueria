package com.example.ProyectoPeluqueria.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirigir rutas del SPA al index.html
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/register").setViewName("forward:/index.html");
        registry.addViewController("/servicios").setViewName("forward:/index.html");
        registry.addViewController("/mis-turnos").setViewName("forward:/index.html");
        registry.addViewController("/perfil").setViewName("forward:/index.html");
        registry.addViewController("/reservar").setViewName("forward:/index.html");
        registry.addViewController("/reservar/**").setViewName("forward:/index.html");
        registry.addViewController("/turnos").setViewName("forward:/index.html");
        registry.addViewController("/turnos/**").setViewName("forward:/index.html");
        registry.addViewController("/admin").setViewName("forward:/index.html");
        registry.addViewController("/admin/**").setViewName("forward:/index.html");
    }
}
