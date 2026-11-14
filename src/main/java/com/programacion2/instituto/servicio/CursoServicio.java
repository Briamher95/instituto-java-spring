package com.programacion2.instituto.servicio;

import java.util.Comparator;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;

import org.springframework.stereotype.Service;

import com.programacion2.instituto.dominio.Curso;
import com.programacion2.instituto.dominio.Inscripcion;
import com.programacion2.instituto.dominio.Profesor;
import com.programacion2.instituto.excepcion.ReglaNegocioException;
import com.programacion2.instituto.repositorio.IRepositorio;

// Registra esta clase como un componente de la capa de servicios.
// Acá viven las reglas de negocio de los cursos.
@Service
public class CursoServicio {

    private final IRepositorio<Curso> repositorio;

    // Se usa para buscar el profesor que se asigna a un curso.
    private final IRepositorio<Profesor> repositorioProfesores;

    // Se usa para borrar las inscripciones al dar de baja un curso.
    private final IRepositorio<Inscripcion> repositorioInscripciones;

    // Spring inyecta los repositorios por el único constructor, sin usar @Autowired.
    // Es el mismo bean genérico Repositorio, visto con tres tipos distintos.
    public CursoServicio(IRepositorio<Curso> repositorio,
                         IRepositorio<Profesor> repositorioProfesores,
                         IRepositorio<Inscripcion> repositorioInscripciones) {
        this.repositorio = repositorio;
        this.repositorioProfesores = repositorioProfesores;
        this.repositorioInscripciones = repositorioInscripciones;
    }

    public List<Curso> listar() {
        return repositorio.listar(Curso.class);
    }

    // Busca por nombre (contiene, sin distinguir mayúsculas). Sin texto, devuelve todos.
    public List<Curso> buscar(String texto) {
        return repositorio.buscar(Curso.class, "nombre", texto);
    }

    public Curso consultar(int id) {
        return repositorio.consultar(Curso.class, id);
    }

    public void guardar(Curso curso) {
        curso.normalizar();
        curso.validar();
        repositorio.guardar(curso);
    }

    public void actualizar(int id, Curso datos) {
        Curso existente = consultar(id);

        // Copiamos solo los datos del formulario: activo, profesor e inscripciones no se tocan.
        existente.setNombre(datos.getNombre());
        existente.setCupoMaximo(datos.getCupoMaximo());

        existente.normalizar();
        existente.validar();

        // No se puede achicar el cupo por debajo de los alumnos ya inscriptos.
        int inscriptos = existente.getInscripciones().size();
        if (existente.getCupoMaximo() < inscriptos) {
            throw new ReglaNegocioException(
                    "El cupo máximo no puede ser menor que la cantidad de inscriptos (" + inscriptos + ")");
        }

        repositorio.guardar(existente);
    }

    // Baja lógica: se borran las inscripciones (libera a los alumnos) y el curso
    // queda inactivo. Conserva su profesor. Reactivarlo no devuelve las inscripciones.
    public void darDeBaja(int id) {
        Curso curso = consultar(id);

        for (Inscripcion inscripcion : curso.getInscripciones()) {
            repositorioInscripciones.eliminar(Inscripcion.class, inscripcion.getId());
        }

        // Volvemos a consultar: la copia anterior todavía tiene en su lista
        // las inscripciones recién borradas. activo cambia al final, así una
        // baja interrumpida se puede repetir.
        curso = consultar(id);
        curso.setActivo(false);
        repositorio.guardar(curso);
    }

    public void reactivar(int id) {
        Curso curso = consultar(id);
        curso.setActivo(true);
        repositorio.guardar(curso);
    }

    // Baja definitiva: solo si no tiene inscripciones. Tener profesor no impide
    // borrarlo: la clave foránea PROFESOR_ID está en la fila del propio curso.
    public void eliminar(int id) {
        Curso curso = consultar(id);

        if (!curso.getInscripciones().isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar: el curso tiene inscripciones");
        }

        repositorio.eliminar(Curso.class, id);
    }

    // Profesores que se pueden elegir al asignar: solo los activos, por apellido y nombre.
    public List<Profesor> profesoresDisponibles() {
        return repositorioProfesores.listar(Profesor.class).stream()
                .filter(Profesor::isActivo)
                .sorted(Comparator.comparing(Profesor::getApellido).thenComparing(Profesor::getNombre))
                .toList();
    }

    // Un curso tiene un solo profesor: asignar uno nuevo reemplaza al anterior.
    public void asignarProfesor(int idCurso, int idProfesor) {
        Curso curso = consultar(idCurso);

        if (!curso.isActivo()) {
            throw new ReglaNegocioException("No se puede asignar un profesor a un curso inactivo");
        }

        Profesor profesor = consultarProfesor(idProfesor);

        if (!profesor.isActivo()) {
            throw new ReglaNegocioException("No se puede asignar un profesor inactivo");
        }

        // Solo cambiamos el lado del curso, que es el dueño de la clave foránea.
        // No usamos Profesor.agregarCurso: la lista de cursos del profesor es el lado
        // inverso (mappedBy) y en el objeto desprendido puede estar desactualizada.
        // DataNucleus la vuelve a armar desde PROFESOR_ID la próxima vez que se consulta.
        curso.setProfesor(profesor);
        repositorio.guardar(curso);
    }

    public void quitarProfesor(int idCurso) {
        Curso curso = consultar(idCurso);

        if (curso.getProfesor() == null) {
            throw new ReglaNegocioException("El curso no tiene profesor asignado");
        }

        // Igual que al asignar: solo se cambia el lado del curso (PROFESOR_ID queda en null).
        curso.setProfesor(null);
        repositorio.guardar(curso);
    }

    // El id llega del formulario: si no existe, avisamos del profesor y no del curso.
    private Profesor consultarProfesor(int idProfesor) {
        try {
            return repositorioProfesores.consultar(Profesor.class, idProfesor);
        } catch (JDOObjectNotFoundException e) {
            throw new ReglaNegocioException("El profesor no existe");
        }
    }
}
