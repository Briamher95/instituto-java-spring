package com.programacion2.instituto.controlador;

import javax.jdo.JDOObjectNotFoundException;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

import com.programacion2.instituto.dominio.Alumno;
import com.programacion2.instituto.excepcion.ReglaNegocioException;
import com.programacion2.instituto.servicio.AlumnoServicio;

import jakarta.servlet.http.HttpServletRequest;

// Todas las rutas de este controlador empiezan con /alumnos.
@Controller
@RequestMapping("/alumnos")
public class AlumnoControlador {

    private final AlumnoServicio servicio;

    // Spring inyecta el servicio por el único constructor, sin usar @Autowired.
    public AlumnoControlador(AlumnoServicio servicio) {
        this.servicio = servicio;
    }

    // Se ejecuta antes de vincular cada formulario de este controlador.
    @InitBinder
    public void configurarVinculacion(WebDataBinder binder) {
        // Quita espacios al inicio y al final, y convierte "" en null.
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));

        // Solo se vinculan estos campos: el formulario no puede cambiar activo ni inscripciones.
        binder.setAllowedFields("dni", "nombre", "apellido", "email", "telefono");
    }

    // GET /alumnos, o GET /alumnos?buscar=texto para filtrar por apellido.
    @GetMapping
    public String listar(@RequestParam(name = "buscar", required = false) String buscar, Model model) {
        model.addAttribute("alumnos", servicio.buscar(buscar));
        // Devolvemos el texto para que siga escrito en la caja de búsqueda.
        model.addAttribute("buscar", buscar);
        return "alumnos";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("alumno", new Alumno());
        return "alumno-form";
    }

    @PostMapping
    public String guardar(
            @ModelAttribute("alumno") Alumno alumno,
            Model model,
            RedirectAttributes redireccion) {

        try {
            // El servicio valida los datos y la regla de DNI único.
            servicio.guardar(alumno);
        } catch (ReglaNegocioException e) {
            // Volvemos al formulario SIN redirect: "alumno" sigue en el modelo
            // con lo que escribió el usuario, y el mensaje se muestra arriba.
            model.addAttribute("error", e.getMessage());
            return "alumno-form";
        }

        // El mensaje flash sobrevive al redirect y se muestra una sola vez.
        redireccion.addFlashAttribute("mensaje", "Alumno registrado correctamente");
        return "redirect:/alumnos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        model.addAttribute("alumno", servicio.consultar(id));
        // El formulario usa idAlumno para saber que es una edición y a dónde enviar.
        model.addAttribute("idAlumno", id);
        return "alumno-form";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable int id,
            @ModelAttribute("alumno") Alumno alumno,
            Model model,
            RedirectAttributes redireccion) {

        try {
            servicio.actualizar(id, alumno);
        } catch (ReglaNegocioException e) {
            // El objeto vinculado no trae id (Persona no tiene setId), por eso lo pasamos aparte.
            model.addAttribute("idAlumno", id);
            model.addAttribute("error", e.getMessage());
            return "alumno-form";
        }

        redireccion.addFlashAttribute("mensaje", "Alumno actualizado correctamente");
        return "redirect:/alumnos";
    }

    @PostMapping("/{id}/baja")
    public String darDeBaja(@PathVariable int id, RedirectAttributes redireccion) {
        servicio.darDeBaja(id);
        redireccion.addFlashAttribute("mensaje", "Alumno dado de baja y sus inscripciones eliminadas");
        return "redirect:/alumnos";
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable int id, RedirectAttributes redireccion) {
        servicio.reactivar(id);
        redireccion.addFlashAttribute("mensaje", "Alumno reactivado");
        return "redirect:/alumnos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable int id, RedirectAttributes redireccion) {
        try {
            servicio.eliminar(id);
            redireccion.addFlashAttribute("mensaje", "Alumno eliminado");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/alumnos";
    }

    // Si el id no existe (por ejemplo, un enlace viejo o un doble clic en Eliminar),
    // volvemos a la lista con un mensaje en lugar de mostrar una página de error.
    @ExceptionHandler(JDOObjectNotFoundException.class)
    public String alumnoInexistente(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", "El alumno no existe");
        return "redirect:/alumnos";
    }
}
