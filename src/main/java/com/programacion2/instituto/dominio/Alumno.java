package com.programacion2.instituto.dominio;

import java.util.ArrayList;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable(detachable = "true")
public class Alumno extends Persona {

    // Lado "uno" de la relación Alumno 1 --- N Inscripcion.
    // mappedBy indica que la relación la guarda el campo "alumno" de Inscripcion
    // (columna ALUMNO_ID en la tabla de inscripciones), no una tabla aparte.
    // @Order con list-ordering: la lista se ordena por id y DataNucleus no
    // agrega una columna de posición en la tabla de inscripciones.
    @Persistent(mappedBy = "alumno", defaultFetchGroup = "true")
    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "id ASC"))
    private ArrayList<Inscripcion> inscripciones;

    // Constructor vacío: lo necesitan JDO y el formulario de Spring MVC.
    public Alumno() {
        super();
        inscripciones = new ArrayList<>();
    }

    public Alumno(String dni, String nombre, String apellido, String email, String telefono) {
        super(dni, nombre, apellido, email, telefono);
        this.inscripciones = new ArrayList<>();
    }

    public ArrayList<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(ArrayList<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    // Agrega la inscripción y completa el otro lado de la relación.
    // Usa el setter de Inscripcion (que no vuelve a llamar acá), así no hay bucle.
    public void agregarInscripcion(Inscripcion inscripcion) {
        if (!inscripciones.contains(inscripcion)) {
            inscripciones.add(inscripcion);
        }
        inscripcion.setAlumno(this);
    }

    // No imprime las inscripciones para evitar recursión entre toString.
    @Override
    public String toString() {
        return "Alumno{" +
                "id=" + getId() +
                ", dni='" + getDni() + '\'' +
                ", nombre='" + getNombre() + '\'' +
                ", apellido='" + getApellido() + '\'' +
                '}';
    }
}
