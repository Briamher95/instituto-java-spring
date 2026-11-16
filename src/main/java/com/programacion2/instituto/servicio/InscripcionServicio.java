package com.programacion2.instituto.servicio;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;

import org.springframework.stereotype.Service;

import com.programacion2.instituto.dominio.Alumno;
import com.programacion2.instituto.dominio.Curso;
import com.programacion2.instituto.dominio.Inscripcion;
import com.programacion2.instituto.excepcion.ReglaNegocioException;
import com.programacion2.instituto.repositorio.IInscripcionRepositorio;
import com.programacion2.instituto.repositorio.IRepositorio;

// Registra esta clase como un componente de la capa de servicios.

@Service
public class InscripcionServicio {

    private final IRepositorio<Alumno> repositorioAlumnos;

    private final IRepositorio<Curso> repositorioCursos;

    private final IRepositorio<Inscripcion> repositorio;

    // Crea la inscripción por ids, sin guardar alumno ni curso desprendidos.
    private final IInscripcionRepositorio repositorioAltas;

    // Spring inyecta los repositorios por el único constructor, sin usar @Autowired.
    public InscripcionServicio(IRepositorio<Alumno> repositorioAlumnos,
                               IRepositorio<Curso> repositorioCursos,
                               IRepositorio<Inscripcion> repositorio,
                               IInscripcionRepositorio repositorioAltas) {
        this.repositorioAlumnos = repositorioAlumnos;
        this.repositorioCursos = repositorioCursos;
        this.repositorio = repositorio;
        this.repositorioAltas = repositorioAltas;
    }

    // Todas las inscripciones, las más recientes primero (a igual fecha, la última creada).
    public List<Inscripcion> listar() {
        return repositorio.listar(Inscripcion.class).stream()
                .sorted(Comparator.comparing(Inscripcion::getFechaInscripcion)
                        .thenComparing(Inscripcion::getId)
                        .reversed())
                .toList();
    }

    // Alumnos que se pueden inscribir: solo los activos, por apellido y nombre.
    public List<Alumno> alumnosDisponibles() {
        return repositorioAlumnos.listar(Alumno.class).stream()
                .filter(Alumno::isActivo)
                .sorted(Comparator.comparing(Alumno::getApellido).thenComparing(Alumno::getNombre))
                .toList();
    }

    // Cursos que pueden recibir alumnos: activos y con cupo, por nombre.
    public List<Curso> cursosDisponibles() {
        return repositorioCursos.listar(Curso.class).stream()
                .filter(curso -> curso.isActivo() && curso.tieneCupo())
                .sorted(Comparator.comparing(Curso::getNombre))
                .toList();
    }

    
    public void inscribir(Integer idAlumno, Integer idCurso) {
        if (idAlumno == null) {
            throw new ReglaNegocioException("Elegí un alumno");
        }
        if (idCurso == null) {
            throw new ReglaNegocioException("Elegí un curso");
        }

        Alumno alumno = consultarAlumno(idAlumno);
        if (!alumno.isActivo()) {
            throw new ReglaNegocioException("No se puede inscribir: el alumno está dado de baja");
        }

        // consultar trae el curso con sus inscripciones (y el alumno de cada una).
        Curso curso = consultarCurso(idCurso);
        if (!curso.isActivo()) {
            throw new ReglaNegocioException("No se puede inscribir: el curso está dado de baja");
        }

        boolean yaInscripto = curso.getInscripciones().stream()
                .anyMatch(inscripcion -> inscripcion.getAlumno().getId() == idAlumno);
        if (yaInscripto) {
            throw new ReglaNegocioException("El alumno ya está inscripto en ese curso");
        }

        if (!curso.tieneCupo()) {
            throw new ReglaNegocioException("El curso no tiene cupo disponible");
        }

        // Se crea por ids
        repositorioAltas.crear(idAlumno, idCurso, LocalDate.now());
    }

    // Borra la fila de la inscripción; siempre está permitido y libera el cupo.
    public void desinscribir(int id) {
        try {
            repositorio.eliminar(Inscripcion.class, id);
        } catch (JDOObjectNotFoundException e) {
            throw new ReglaNegocioException("La inscripción no existe");
        }
    }

    // Los ids llegan del formulario: si no existen, avisamos con un mensaje claro.
    private Alumno consultarAlumno(int idAlumno) {
        try {
            return repositorioAlumnos.consultar(Alumno.class, idAlumno);
        } catch (JDOObjectNotFoundException e) {
            throw new ReglaNegocioException("El alumno no existe");
        }
    }

    private Curso consultarCurso(int idCurso) {
        try {
            return repositorioCursos.consultar(Curso.class, idCurso);
        } catch (JDOObjectNotFoundException e) {
            throw new ReglaNegocioException("El curso no existe");
        }
    }
}
