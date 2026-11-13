package com.programacion2.instituto.dominio;

import java.time.LocalDate;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;

// Entidad intermedia entre Alumno y Curso, con su propia tabla.
// Si la fila existe, el alumno está inscripto en el curso.
@PersistenceCapable(detachable = "true")
// Restricción única sobre el par: un alumno no puede inscribirse dos veces al mismo curso.
@Unique(members = {"curso", "alumno"})
public class Inscripcion {

    // El identificador lo genera la base de datos (autoincremental).
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    // Lado "muchos" de Curso 1 --- N Inscripcion: acá vive la clave foránea.
    @Persistent(defaultFetchGroup = "true")
    @Column(name = "CURSO_ID", allowsNull = "false")
    private Curso curso;

    // Lado "muchos" de Alumno 1 --- N Inscripcion: acá vive la clave foránea.
    @Persistent(defaultFetchGroup = "true")
    @Column(name = "ALUMNO_ID", allowsNull = "false")
    private Alumno alumno;

    @Column(allowsNull = "false")
    private LocalDate fechaInscripcion;

    // Constructor vacío: lo necesita JDO.
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

    // Setter simple: no toca la lista del curso (eso lo hace Curso.agregarInscripcion).
    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    // Setter simple: no toca la lista del alumno (eso lo hace Alumno.agregarInscripcion).
    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    // No imprime el curso ni el alumno completos para evitar recursión entre toString.
    @Override
    public String toString() {
        return "Inscripcion{" +
                "id=" + id +
                ", fechaInscripcion=" + fechaInscripcion +
                '}';
    }
}
