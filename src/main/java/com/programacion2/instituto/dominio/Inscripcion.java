package com.programacion2.instituto.dominio;

import java.time.LocalDate;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;

// Entidad intermedia entre Alumno y Curso, con su propia tabla.

@PersistenceCapable(detachable = "true")

@Unique(members = {"curso", "alumno"})
public class Inscripcion {


    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    @Persistent(defaultFetchGroup = "true")
    @Column(name = "CURSO_ID", allowsNull = "false")
    private Curso curso;


    @Persistent(defaultFetchGroup = "true")
    @Column(name = "ALUMNO_ID", allowsNull = "false")
    private Alumno alumno;

    @Column(allowsNull = "false")
    private LocalDate fechaInscripcion;

  
    public Inscripcion() {
    }

    public Inscripcion(Curso curso, Alumno alumno, LocalDate fechaInscripcion) {
        this.curso = curso;
        this.alumno = alumno;
        this.fechaInscripcion = fechaInscripcion;
    }

    public int getId() {
        return id;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }


    @Override
    public String toString() {
        return "Inscripcion{" +
                "id=" + id +
                ", fechaInscripcion=" + fechaInscripcion +
                '}';
    }
}
