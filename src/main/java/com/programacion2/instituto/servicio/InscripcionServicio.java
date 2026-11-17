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



@Service
public class InscripcionServicio {

    private final IRepositorio<Alumno> repositorioAlumnos;

    private final IRepositorio<Curso> repositorioCursos;

    private final IRepositorio<Inscripcion> repositorio;


    private final IInscripcionRepositorio repositorioAltas;


    public InscripcionServicio(IRepositorio<Alumno> repositorioAlumnos,
                               IRepositorio<Curso> repositorioCursos,
                               IRepositorio<Inscripcion> repositorio,
                               IInscripcionRepositorio repositorioAltas) {
        this.repositorioAlumnos = repositorioAlumnos;
        this.repositorioCursos = repositorioCursos;
        this.repositorio = repositorio;
        this.repositorioAltas = repositorioAltas;
    }

    public List<Inscripcion> listar() {
        return repositorio.listar(Inscripcion.class).stream()
                .sorted(Comparator.comparing(Inscripcion::getFechaInscripcion)
                        .thenComparing(Inscripcion::getId)
                        .reversed())
                .toList();
    }

    public List<Alumno> alumnosDisponibles() {
        return repositorioAlumnos.listar(Alumno.class).stream()
                .filter(Alumno::isActivo)
                .sorted(Comparator.comparing(Alumno::getApellido).thenComparing(Alumno::getNombre))
                .toList();
    }

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

        
        repositorioAltas.crear(idAlumno, idCurso, LocalDate.now());
    }

    // Borra la fila de la inscripción;
    public void desinscribir(int id) {
        try {
            repositorio.eliminar(Inscripcion.class, id);
        } catch (JDOObjectNotFoundException e) {
            throw new ReglaNegocioException("La inscripción no existe");
        }
    }

    // Los ids llegan del formulario
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
