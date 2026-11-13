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

import com.programacion2.instituto.dominio.Profesor;
import com.programacion2.instituto.excepcion.ReglaNegocioException;
import com.programacion2.instituto.servicio.ProfesorServicio;

import jakarta.servlet.http.HttpServletRequest;

// Todas las rutas de este controlador empiezan con /profesores.
@Controller
@RequestMapping("/profesores")
public class ProfesorControlador {

    private final ProfesorServicio servicio;

    // Spring inyecta el servicio por el único constructor, sin usar @Autowired.
    public ProfesorControlador(ProfesorServicio servicio) {
        this.servicio = servicio;
    }

    // Se ejecuta antes de vincular cada formulario de este controlador.
    @InitBinder
    public void configurarVinculacion(WebDataBinder binder) {
        // Quita espacios al inicio y al final, y convierte "" en null.
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));

        // Solo se vinculan estos campos: el formulario no puede cambiar activo ni cursos.
        binder.setAllowedFields("dni", "nombre", "apellido", "email", "telefono");
    }

    // GET /profesores, o GET /profesores?buscar=texto para filtrar por apellido.
    @GetMapping
    public String listar(@RequestParam(name = "buscar", required = false) String buscar, Model model) {
        model.addAttribute("profesores", servicio.buscar(buscar));
        // Devolvemos el texto para que siga escrito en la caja de búsqueda.
        model.addAttribute("buscar", buscar);
        return "profesores";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("profesor", new Profesor());
        return "profesor-form";
    }

    @PostMapping
    public String guardar(
            @ModelAttribute("profesor") Profesor profesor,
            Model model,
            RedirectAttributes redireccion) {

        try {
            // El servicio valida los datos y la regla de DNI único.
            servicio.guardar(profesor);
        } catch (ReglaNegocioException e) {
            // Volvemos al formulario SIN redirect: "profesor" sigue en el modelo
            // con lo que escribió el usuario, y el mensaje se muestra arriba.
            model.addAttribute("error", e.getMessage());
            return "profesor-form";
        }

        // El mensaje flash sobrevive al redirect y se muestra una sola vez.
        redireccion.addFlashAttribute("mensaje", "Profesor registrado correctamente");
        return "redirect:/profesores";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        model.addAttribute("profesor", servicio.consultar(id));
        // El formulario usa idProfesor para saber que es una edición y a dónde enviar.
        model.addAttribute("idProfesor", id);
        return "profesor-form";
    }

    @PostMapping("/{id}")
    public String actualizar(
            @PathVariable int id,
            @ModelAttribute("profesor") Profesor profesor,
            Model model,
            RedirectAttributes redireccion) {

        try {
            servicio.actualizar(id, profesor);
        } catch (ReglaNegocioException e) {
            // El objeto vinculado no trae id (Persona no tiene setId), por eso lo pasamos aparte.
            model.addAttribute("idProfesor", id);
            model.addAttribute("error", e.getMessage());
            return "profesor-form";
        }

        redireccion.addFlashAttribute("mensaje", "Profesor actualizado correctamente");
        return "redirect:/profesores";
    }

    @PostMapping("/{id}/baja")
    public String darDeBaja(@PathVariable int id, RedirectAttributes redireccion) {
        servicio.darDeBaja(id);
        redireccion.addFlashAttribute("mensaje", "Profesor dado de baja");
        return "redirect:/profesores";
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable int id, RedirectAttributes redireccion) {
        servicio.reactivar(id);
        redireccion.addFlashAttribute("mensaje", "Profesor reactivado");
        return "redirect:/profesores";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable int id, RedirectAttributes redireccion) {
        try {
            servicio.eliminar(id);
            redireccion.addFlashAttribute("mensaje", "Profesor eliminado");
        } catch (ReglaNegocioException e) {
            redireccion.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profesores";
    }

    // Si el id no existe (por ejemplo, un enlace viejo o un doble clic en Eliminar),
    // volvemos a la lista con un mensaje en lugar de mostrar una página de error.
    @ExceptionHandler(JDOObjectNotFoundException.class)
    public String profesorInexistente(HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", "El profesor no existe");
        return "redirect:/profesores";
    }
}
