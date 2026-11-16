package com.programacion2.instituto.dominio;

import java.util.ArrayList;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable(detachable = "true")
public class Alumno extends Persona {

    // Lado "uno" de la relación Alumno 1 --- N Inscripcion.
  
    @Persistent(mappedBy = "alumno", defaultFetchGroup = "true")
    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "id ASC"))
    private ArrayList<Inscripcion> inscripciones;

 
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
