package com.programacion2.instituto.dominio;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable(detachable = "true")
public class Alumno extends Persona {

    // Constructor vacío: lo necesitan JDO y el formulario de Spring MVC.
    public Alumno() {
        super();
    }

    public Alumno(String dni, String nombre, String apellido, String email, String telefono) {
        super(dni, nombre, apellido, email, telefono);
    }

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
