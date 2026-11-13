package com.programacion2.instituto.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.programacion2.instituto.dominio.Alumno;
import com.programacion2.instituto.servicio.AlumnoServicio;

@Controller
public class AlumnoControlador {

    private final AlumnoServicio servicio;

    // Spring inyecta el servicio por el único constructor, sin usar @Autowired.
    public AlumnoControlador(AlumnoServicio servicio) {
        this.servicio = servicio;
    }

    // Atiende GET /alumnos y muestra la lista en alumnos.html.
    @GetMapping("/alumnos")
    public String listar(Model model) {
        // Deja la lista disponible para Thymeleaf con el nombre "alumnos".
        model.addAttribute("alumnos", servicio.listar());
        return "alumnos";
    }

    // Atiende GET /alumnos/nuevo y abre el formulario alumno-form.html.
    @GetMapping("/alumnos/nuevo")
    public String nuevo(Model model) {
        // Proporciona un objeto vacío para vincularlo con el formulario.
        model.addAttribute("alumno", new Alumno());
        return "alumno-form";
    }

    // Atiende POST /alumnos cuando se envía el formulario.
    @PostMapping("/alumnos")
    public String guardar(
            // Vincula los campos enviados con las propiedades de Alumno.
            @ModelAttribute("alumno") Alumno alumno) {

        servicio.guardar(alumno);

        // Hace que el navegador vuelva a pedir el listado con un GET.
        return "redirect:/alumnos";
    }
}
