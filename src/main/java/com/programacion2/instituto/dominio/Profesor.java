package com.programacion2.instituto.dominio;

import java.util.ArrayList;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable(detachable = "true")
public class Profesor extends Persona {



    @Persistent(mappedBy = "profesor", defaultFetchGroup = "true")
    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "id ASC"))
    private ArrayList<Curso> cursos;

    
    public Profesor() {
        super();
        cursos = new ArrayList<>();
    }

    public Profesor(String dni, String nombre, String apellido, String email, String telefono) {
        super(dni, nombre, apellido, email, telefono);
        this.cursos = new ArrayList<>();
    }

    public ArrayList<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(ArrayList<Curso> cursos) {
        this.cursos = cursos;
    }

   
    
    public void agregarCurso(Curso curso) {
        if (!cursos.contains(curso)) {
            cursos.add(curso);
        }
        curso.setProfesor(this);
    }


    @Override
    public String toString() {
        return "Profesor{" +
                "id=" + getId() +
                ", dni='" + getDni() + '\'' +
                ", nombre='" + getNombre() + '\'' +
                ", apellido='" + getApellido() + '\'' +
                '}';
    }
}
