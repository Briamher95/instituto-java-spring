package com.programacion2.instituto.dominio;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;

// detachable permite usar el objeto fuera del PersistenceManager.
@PersistenceCapable(detachable = "true")
// COMPLETE_TABLE: cada subclase concreta tiene su propia tabla con todas las columnas.
// Las anotaciones @Column de esta clase se aplican en las tablas de Alumno y de Profesor.
@Inheritance(strategy = InheritanceStrategy.COMPLETE_TABLE)
public abstract class Persona {

    // El identificador lo genera la base de datos (autoincremental).
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    // No puede haber dos personas con el mismo DNI (dentro de cada tabla).
    // allowsNull = "false" genera la columna como NOT NULL.
    @Unique
    @Column(allowsNull = "false", length = 20)
    private String dni;

    @Column(allowsNull = "false", length = 100)
    private String nombre;

    @Column(allowsNull = "false", length = 100)
    private String apellido;

    // Opcional: la columna admite null.
    @Column(allowsNull = "true", length = 100)
    private String email;

    @Column(allowsNull = "true", length = 20)
    private String telefono;

    // Por defecto, toda persona nueva está activa.
    // Al ser un boolean primitivo, la columna no admite null.
    private boolean activo = true;

    public Persona() {
    }

    public Persona(String dni, String nombre, String apellido, String email, String telefono) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
    }

    public int getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", dni='" + dni + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                '}';
    }
}
