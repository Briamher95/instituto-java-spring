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



@Service
public class CursoServicio {

    private final IRepositorio<Curso> repositorio;


    private final IRepositorio<Profesor> repositorioProfesores;


    private final IRepositorio<Inscripcion> repositorioInscripciones;


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


        existente.setNombre(datos.getNombre());
        existente.setCupoMaximo(datos.getCupoMaximo());

        existente.normalizar();
        existente.validar();


        int inscriptos = existente.getInscripciones().size();
        if (existente.getCupoMaximo() < inscriptos) {
            throw new ReglaNegocioException(
                    "El cupo máximo no puede ser menor que la cantidad de inscriptos (" + inscriptos + ")");
        }

        repositorio.guardar(existente);
    }


    public void darDeBaja(int id) {
        Curso curso = consultar(id);

        for (Inscripcion inscripcion : curso.getInscripciones()) {
            repositorioInscripciones.eliminar(Inscripcion.class, inscripcion.getId());
        }

   
        curso = consultar(id);
        curso.setActivo(false);
        repositorio.guardar(curso);
    }

    public void reactivar(int id) {
        Curso curso = consultar(id);
        curso.setActivo(true);
        repositorio.guardar(curso);
    }

    public void eliminar(int id) {
        Curso curso = consultar(id);

        if (!curso.getInscripciones().isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar: el curso tiene inscripciones");
        }

        repositorio.eliminar(Curso.class, id);
    }

    public List<Profesor> profesoresDisponibles() {
        return repositorioProfesores.listar(Profesor.class).stream()
                .filter(Profesor::isActivo)
                .sorted(Comparator.comparing(Profesor::getApellido).thenComparing(Profesor::getNombre))
                .toList();
    }

    public void asignarProfesor(int idCurso, int idProfesor) {
        Curso curso = consultar(idCurso);

        if (!curso.isActivo()) {
            throw new ReglaNegocioException("No se puede asignar un profesor a un curso inactivo");
        }

        Profesor profesor = consultarProfesor(idProfesor);

        if (!profesor.isActivo()) {
            throw new ReglaNegocioException("No se puede asignar un profesor inactivo");
        }

        curso.setProfesor(profesor);
        repositorio.guardar(curso);
    }

    public void quitarProfesor(int idCurso) {
        Curso curso = consultar(idCurso);

        if (curso.getProfesor() == null) {
            throw new ReglaNegocioException("El curso no tiene profesor asignado");
        }


        curso.setProfesor(null);
        repositorio.guardar(curso);
    }

    private Profesor consultarProfesor(int idProfesor) {
        try {
            return repositorioProfesores.consultar(Profesor.class, idProfesor);
        } catch (JDOObjectNotFoundException e) {
            throw new ReglaNegocioException("El profesor no existe");
        }
    }
}
