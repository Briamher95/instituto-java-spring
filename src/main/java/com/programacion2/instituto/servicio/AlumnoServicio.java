package com.programacion2.instituto.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.programacion2.instituto.dominio.Alumno;
import com.programacion2.instituto.dominio.Inscripcion;
import com.programacion2.instituto.excepcion.ReglaNegocioException;
import com.programacion2.instituto.repositorio.IRepositorio;


@Service
public class AlumnoServicio {

    private final IRepositorio<Alumno> repositorio;


    private final IRepositorio<Inscripcion> repositorioInscripciones;

 
    public AlumnoServicio(IRepositorio<Alumno> repositorio,
                          IRepositorio<Inscripcion> repositorioInscripciones) {
        this.repositorio = repositorio;
        this.repositorioInscripciones = repositorioInscripciones;
    }

    public List<Alumno> listar() {
        return repositorio.listar(Alumno.class);
    }

    public List<Alumno> buscar(String texto) {
        return repositorio.buscar(Alumno.class, "apellido", texto);
    }

    public Alumno consultar(int id) {
        return repositorio.consultar(Alumno.class, id);
    }

    public void guardar(Alumno alumno) {
        alumno.normalizar();
        alumno.validar();


        verificarDniLibre(alumno.getDni(), 0);

        repositorio.guardar(alumno);
    }

    public void actualizar(int id, Alumno datos) {
        Alumno existente = consultar(id);

  
        existente.setDni(datos.getDni());
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setEmail(datos.getEmail());
        existente.setTelefono(datos.getTelefono());

        existente.normalizar();
        existente.validar();


        verificarDniLibre(existente.getDni(), id);

        repositorio.guardar(existente);
    }


    public void darDeBaja(int id) {
        Alumno alumno = consultar(id);

        for (Inscripcion inscripcion : alumno.getInscripciones()) {
            repositorioInscripciones.eliminar(Inscripcion.class, inscripcion.getId());
        }


        alumno = consultar(id);
        alumno.setActivo(false);
        repositorio.guardar(alumno);
    }

    public void reactivar(int id) {
        Alumno alumno = consultar(id);
        alumno.setActivo(true);
        repositorio.guardar(alumno);
    }

    
    public void eliminar(int id) {
        Alumno alumno = consultar(id);

        if (!alumno.getInscripciones().isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar: el alumno tiene inscripciones");
        }

        repositorio.eliminar(Alumno.class, id);
    }

   
    private void verificarDniLibre(String dni, int idExcluido) {
        boolean enUso = repositorio.buscar(Alumno.class, "dni", dni).stream()
                .anyMatch(otro -> otro.getDni().equals(dni) && otro.getId() != idExcluido);

        if (enUso) {
            throw new ReglaNegocioException("Ya existe un alumno con ese DNI");
        }
    }
}
