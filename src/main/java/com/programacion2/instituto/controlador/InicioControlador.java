package com.programacion2.instituto.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Registra esta clase como un controlador de Spring MVC.
@Controller
public class InicioControlador {

    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }
}
