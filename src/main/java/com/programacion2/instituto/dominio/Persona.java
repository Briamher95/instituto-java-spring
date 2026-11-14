package com.programacion2.instituto.dominio;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;

import com.programacion2.instituto.excepcion.ReglaNegocioException;

// detachable permite usar el objeto fuera del PersistenceManager.
@PersistenceCapable(detachable = "true")
// COMPLETE_TABLE: cada subclase concreta tiene su propia tabla con todas las columnas.
// Las anotaciones @Column de esta clase se aplican en las tablas de Alumno y de Profesor.
@Inheritance(strategy = InheritanceStrategy.COMPLETE_TABLE)
public abstract class Persona {

    // Largos máximos que revisa validar(); coinciden con los length de @Column.
    // Al ser static, DataNucleus no los guarda en la base.
    private static final int LARGO_DNI = 20;
    private static final int LARGO_NOMBRE = 100;
    private static final int LARGO_APELLIDO = 100;
    private static final int LARGO_EMAIL = 100;
    private static final int LARGO_TELEFONO = 20;

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

    // El formulario manda "" en los campos vacíos: quitamos espacios sobrantes
    // y los campos vacíos quedan en null.
    public void normalizar() {
        setDni(vacioANull(getDni()));
        setNombre(vacioANull(getNombre()));
        setApellido(vacioANull(getApellido()));
        setEmail(vacioANull(getEmail()));
        setTelefono(vacioANull(getTelefono()));
    }

    // Se llama después de normalizar, así un campo con solo espacios ya es null.
    // Corta en el primer error: el mensaje nombra el campo que hay que corregir.
    public void validar() {
        obligatorio(getDni(), "El DNI es obligatorio");
        obligatorio(getNombre(), "El nombre es obligatorio");
        obligatorio(getApellido(), "El apellido es obligatorio");

        largoMaximo(getDni(), LARGO_DNI, "El DNI no puede superar los 20 caracteres");
        largoMaximo(getNombre(), LARGO_NOMBRE, "El nombre no puede superar los 100 caracteres");
        largoMaximo(getApellido(), LARGO_APELLIDO, "El apellido no puede superar los 100 caracteres");
        largoMaximo(getEmail(), LARGO_EMAIL, "El email no puede superar los 100 caracteres");
        largoMaximo(getTelefono(), LARGO_TELEFONO, "El teléfono no puede superar los 20 caracteres");

        // El email es opcional: solo se revisa si viene.
        if (getEmail() != null && !emailValido(getEmail())) {
            throw new ReglaNegocioException("El email no tiene un formato válido");
        }
    }

    private void obligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    private void largoMaximo(String valor, int maximo, String mensaje) {
        if (valor != null && valor.length() > maximo) {
            throw new ReglaNegocioException(mensaje);
        }
    }

    // Chequeo simple, sin expresiones regulares: sin espacios, una sola @,
    // texto antes y después, y un punto en la parte de después (ej.: ana@correo.com).
    private boolean emailValido(String email) {
        int arroba = email.indexOf('@');

        if (email.contains(" ") || arroba <= 0 || arroba != email.lastIndexOf('@')) {
            return false;
        }

        String dominio = email.substring(arroba + 1);
        int punto = dominio.indexOf('.');

        // El punto no puede ir pegado a la @ ni al final (ana@.com o ana@correo.).
        return punto > 0 && !dominio.endsWith(".");
    }

    private String vacioANull(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
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
