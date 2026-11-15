package com.programacion2.instituto.controlador;

import javax.jdo.JDOObjectNotFoundException;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.programacion2.instituto.dominio.Curso;
import com.programacion2.instituto.excepcion.ReglaNegocioException;
import com.programacion2.instituto.servicio.CursoServicio;

import jakarta.servlet.http.HttpServletRequest;

// Todas las rutas de este controlador empiezan con /cursos.
@Controller
@RequestMapping("/cursos")
public class CursoControlador {

 
    private static final String ERROR_CUPO = "El cupo máximo debe ser un número entero mayor a 0";

    private final CursoServicio servicio;

    // Spring inyecta el servicio por el único constructor, sin usar @Autowired.
    public CursoControlador(CursoServicio servicio) {
        this.servicio = servicio;
    }

    
    @InitBinder
    public void configurarVinculacion(WebDataBinder binder) {
    
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));

        binder.setAllowedFields("nombre", "cupoMaximo");
    }

    // GET /cursos, o GET /cursos?buscar=texto para filtrar por nombre.
    @GetMapping
    public String listar(@RequestParam(name = "buscar", required = false) String buscar, Model model) {
        model.addAttribute("cursos", servicio.buscar(buscar));
        // Devolvemos el texto para que siga escrito en la caja de búsqueda.
        model.addAttribute("buscar", buscar);
        return "cursos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("curso", new Curso());
        return "curso-form";
    }

   
    @PostMapping
    public String guardar(
            @ModelAttribute("curso") Curso curso,
            BindingResult resultado,
            Model model,
            RedirectAttributes redireccion) {

        if (resultado.hasFieldErrors("cupoMaximo")) {
            model.addAttribute("error", ERROR_CUPO);
            return "curso-form";
        }

        try {
            servicio.guardar(curso);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "curso-form";
        }

        // El mensaje flash sobrevive al redirect y se muestra una sola vez.
        redireccion.addFlashAttribute("mensaje", "Curso registrado correctamente");
        return "redirect:/cursos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        model.addAttribute("curso", servicio.consultar(id));
    
        model.addAttribute("idCurso", id);
        return "curso-form";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable int id,
            @ModelAttribute("curso") Curso curso,
            BindingResult resultado,
            Model model,
            RedirectAttributes redireccion) {

        // El objeto vinculado no trae id (Curso no tiene setId), por eso lo pasamos aparte.
        model.addAttribute("idCurso", id);

        if (resultado.hasFieldErrors("cupoMaximo")) {
            model.addAttribute("error", ERROR_CUPO);
            return "curso-form";
        }

        try {
            servicio.actualizar(id, curso);
        } catch (ReglaNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "curso-form";
        }

        redireccion.addFlashAttribute("mensaje", "Curso actualizado correctamente");
        return "redirect:/cursos";
    }

    @PostMapping("/{id}/baja")
    public String darDeBaja(@PathVariable int id, RedirectAttributes redireccion) {
        servicio.darDeBaja(id);
        redireccion.addFlashAttribute("mensaje", "Curso dado de baja y sus inscripciones eliminadas");
        return "redirect:/cursos";
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable int id, RedirectAttributes redireccion) {
        servicio.reactivar(id);
        redireccion.addFlashAttribute("mensaje", "Curso reactivado");
        return "redirect:/cursos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable int id, RedirectAttributes redireccion) {
        try {
            servicio.eliminar(id);
            redireccion.addFlashAttribute("mensaje", "Curso eliminado");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cursos";
    }

    // Pantalla para elegir el profesor del curso entre los profesores activos.
    @GetMapping("/{id}/profesor")
    public String elegirProfesor(@PathVariable int id, Model model) {
        cargarPantallaProfesor(id, model);
        return "curso-profesor";
    }

    // required = false: si no se eligió ningún profesor, avisamos en la misma pantalla.
    @PostMapping("/{id}/profesor")
    public String asignarProfesor(
            @PathVariable int id,
            @RequestParam(name = "idProfesor", required = false) Integer idProfesor,
            Model model,
            RedirectAttributes redireccion) {

        try {
            if (idProfesor == null) {
                throw new ReglaNegocioException("Elegí un profesor de la lista");
            }
            servicio.asignarProfesor(id, idProfesor);
        } catch (ReglaNegocioException e) {
            // Volvemos a la pantalla SIN redirect, con el mensaje arriba.
            cargarPantallaProfesor(id, model);
            model.addAttribute("error", e.getMessage());
            return "curso-profesor";
        }

        redireccion.addFlashAttribute("mensaje", "Profesor asignado correctamente");
        return "redirect:/cursos";
    }

    @PostMapping("/{id}/profesor/quitar")
    public String quitarProfesor(@PathVariable int id, RedirectAttributes redireccion) {
        try {
            servicio.quitarProfesor(id);
            redireccion.addFlashAttribute("mensaje", "Profesor quitado del curso");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cursos";
    }


    private void cargarPantallaProfesor(int id, Model model) {
        model.addAttribute("curso", servicio.consultar(id));
        model.addAttribute("profesores", servicio.profesoresDisponibles());
    }


    @ExceptionHandler(JDOObjectNotFoundException.class)
    public String cursoInexistente(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", "El curso no existe");
        return "redirect:/cursos";
    }
}
