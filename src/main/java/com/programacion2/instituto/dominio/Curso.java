package com.programacion2.instituto.dominio;

import java.util.ArrayList;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.programacion2.instituto.excepcion.ReglaNegocioException;

@PersistenceCapable(detachable = "true")
public class Curso {


    private static final int LARGO_NOMBRE = 50;

   
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    @Column(allowsNull = "false", length = 50)
    private String nombre;


    private int cupoMaximo;

    private boolean activo = true;


    @Persistent(defaultFetchGroup = "true")
    @Column(name = "PROFESOR_ID", allowsNull = "true")
    private Profesor profesor;

  
    @Persistent(mappedBy = "curso", defaultFetchGroup = "true")
    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "id ASC"))
    private ArrayList<Inscripcion> inscripciones;


    public Curso() {
        inscripciones = new ArrayList<>();
    }

    public Curso(String nombre, int cupoMaximo) {
        this.nombre = nombre;
        this.cupoMaximo = cupoMaximo;
        this.inscripciones = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Profesor getProfesor() {
        return profesor;
    }


    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public ArrayList<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(ArrayList<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }


    public void agregarInscripcion(Inscripcion inscripcion) {
        if (!inscripciones.contains(inscripcion)) {
            inscripciones.add(inscripcion);
        }
        inscripcion.setCurso(this);
    }


    public int cupoDisponible() {
        return cupoMaximo - inscripciones.size();
    }

    public boolean tieneCupo() {
        return cupoDisponible() > 0;
    }


    public void normalizar() {
        if (nombre == null || nombre.isBlank()) {
            nombre = null;
        } else {
            nombre = nombre.trim();
        }
    }


    public void validar() {
        if (nombre == null) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }

        if (nombre.length() > LARGO_NOMBRE) {
            throw new ReglaNegocioException("El nombre no puede superar los 50 caracteres");
        }

        if (cupoMaximo <= 0) {
            throw new ReglaNegocioException("El cupo máximo debe ser un número entero mayor a 0");
        }
    }


    @Override
    public String toString() {
        return "Curso{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", cupoMaximo=" + cupoMaximo +
                ", activo=" + activo +
                '}';
    }
}
