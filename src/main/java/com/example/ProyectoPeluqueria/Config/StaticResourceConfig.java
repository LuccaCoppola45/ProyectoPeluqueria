package com.example.ProyectoPeluqueria.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/register").setViewName("forward:/index.html");
        registry.addViewController("/servicios").setViewName("forward:/index.html");
        registry.addViewController("/turnos").setViewName("forward:/index.html");
        registry.addViewController("/turnos/**").setViewName("forward:/index.html");
        registry.addViewController("/mis-turnos").setViewName("forward:/index.html");
        registry.addViewController("/mis-turnos/**").setViewName("forward:/index.html");
        registry.addViewController("/reservar").setViewName("forward:/index.html");
        registry.addViewController("/reservar/**").setViewName("forward:/index.html");
        registry.addViewController("/perfil").setViewName("forward:/index.html");
        registry.addViewController("/perfil/**").setViewName("forward:/index.html");
        registry.addViewController("/admin").setViewName("forward:/index.html");
        registry.addViewController("/admin/**").setViewName("forward:/index.html");
    }
}
