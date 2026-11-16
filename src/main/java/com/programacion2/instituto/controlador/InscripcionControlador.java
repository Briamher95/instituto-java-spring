package com.programacion2.instituto.controlador;

import javax.jdo.JDOObjectNotFoundException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.programacion2.instituto.excepcion.ReglaNegocioException;
import com.programacion2.instituto.servicio.InscripcionServicio;

import jakarta.servlet.http.HttpServletRequest;

// Todas las rutas de este controlador empiezan con /inscripciones.
@Controller
@RequestMapping("/inscripciones")
public class InscripcionControlador {

    private final InscripcionServicio servicio;


    public InscripcionControlador(InscripcionServicio servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("alumnos", servicio.alumnosDisponibles());
        model.addAttribute("cursos", servicio.cursosDisponibles());
        model.addAttribute("inscripciones", servicio.listar());
        return "inscripciones";
    }

    // required = false: un select vacío llega sin valor y el servicio avisa con un mensaje
    // (en lugar de que Spring responda con un error 400).
    @PostMapping
    public String inscribir(
            @RequestParam(name = "idAlumno", required = false) Integer idAlumno,
            @RequestParam(name = "idCurso", required = false) Integer idCurso,
            RedirectAttributes redireccion) {

        try {
            servicio.inscribir(idAlumno, idCurso);
            redireccion.addFlashAttribute("mensaje", "Alumno inscripto correctamente");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inscripciones";
    }

    @PostMapping("/{id}/eliminar")
    public String desinscribir(@PathVariable int id, RedirectAttributes redireccion) {
        try {
            servicio.desinscribir(id);
            redireccion.addFlashAttribute("mensaje", "Inscripción eliminada");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/inscripciones";
    }

  
    @ExceptionHandler(JDOObjectNotFoundException.class)
    public String inscripcionInexistente(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", "La inscripción no existe");
        return "redirect:/inscripciones";
    }
}
