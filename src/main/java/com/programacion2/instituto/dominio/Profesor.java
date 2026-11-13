package com.programacion2.instituto.dominio;

import java.util.ArrayList;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable(detachable = "true")
public class Profesor extends Persona {

    // Lado "uno" de la relación Profesor 1 --- N Curso.
    // La relación la guarda el campo "profesor" de Curso (columna PROFESOR_ID).
    // @Order con list-ordering: la lista se ordena por id y DataNucleus no
    // agrega una columna de posición en la tabla de cursos.
    @Persistent(mappedBy = "profesor", defaultFetchGroup = "true")
    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "id ASC"))
    private ArrayList<Curso> cursos;

    // Constructor vacío: lo necesitan JDO y los formularios de Spring MVC.
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

    // Agrega el curso y completa el otro lado de la relación.
    // Usa el setter de Curso (que no vuelve a llamar acá), así no hay bucle.
    public void agregarCurso(Curso curso) {
        if (!cursos.contains(curso)) {
            cursos.add(curso);
        }
        curso.setProfesor(this);
    }

    // No imprime los cursos para evitar recursión entre toString.
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
