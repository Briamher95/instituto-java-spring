package com.programacion2.instituto.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Registra esta clase como un controlador de Spring MVC.
@Controller
public class InicioControlador {

    // Atiende GET / y devuelve la vista inicio.html.
    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }
}
