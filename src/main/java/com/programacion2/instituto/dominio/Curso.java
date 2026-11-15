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

    // Largo máximo que revisa validar(); coincide con el length de @Column.
    // Al ser static, DataNucleus no lo guarda en la base.
    private static final int LARGO_NOMBRE = 50;

   
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private int id;

    @Column(allowsNull = "false", length = 50)
    private String nombre;

    // int primitivo: la columna no admite null.
    private int cupoMaximo;

    // Por defecto, todo curso nuevo está activo.
    private boolean activo = true;

    // Lado muchos digamos  de Profesor 1 --- N 
    @Persistent(defaultFetchGroup = "true")
    @Column(name = "PROFESOR_ID", allowsNull = "true")
    private Profesor profesor;

    // Lado "uno" de Curso 1 --- N Inscripcion.
  
    @Persistent(mappedBy = "curso", defaultFetchGroup = "true")
    @Order(extensions = @Extension(vendorName = "datanucleus", key = "list-ordering", value = "id ASC"))
    private ArrayList<Inscripcion> inscripciones;

    // Constructor vacío: lo necesitan JDO y los formularios de Spring MVC.
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

    // Setter simple: no toca la lista del profesor (eso lo hace Profesor.agregarCurso).
    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
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
        inscripcion.setCurso(this);
    }

    // Valor calculado: no es un campo, así que no se guarda en la base.
    public int cupoDisponible() {
        return cupoMaximo - inscripciones.size();
    }

    public boolean tieneCupo() {
        return cupoDisponible() > 0;
    }

    // El formulario manda "" en los campos vacíos: quitamos espacios sobrantes
    // y el nombre vacío queda en null.
    public void normalizar() {
        if (nombre == null || nombre.isBlank()) {
            nombre = null;
        } else {
            nombre = nombre.trim();
        }
    }

    // Se llama después de normalizar, así un nombre con solo espacios ya es null.
    // Corta en el primer error: el mensaje nombra el campo que hay que corregir.
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

    // No imprime el profesor ni las inscripciones para evitar recursión entre toString.
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
