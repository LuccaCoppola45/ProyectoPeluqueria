package com.example.ProyectoPeluqueria.Config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping(value = {
            "/",
            "/{path:^(?!api$)(?!api/).*}",
            "/{path:^(?!api$)(?!api/).*}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
