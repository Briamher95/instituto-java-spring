package com.programacion2.instituto.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.programacion2.instituto.dominio.Profesor;
import com.programacion2.instituto.excepcion.ReglaNegocioException;
import com.programacion2.instituto.repositorio.IRepositorio;

// Registra esta clase como un componente de la capa de servicios.
// Acá viven las reglas de negocio de los profesores.
@Service
public class ProfesorServicio {

    private final IRepositorio<Profesor> repositorio;

    // Spring inyecta el repositorio por el único constructor, sin usar @Autowired.
    public ProfesorServicio(IRepositorio<Profesor> repositorio) {
        this.repositorio = repositorio;
    }

    public List<Profesor> listar() {
        return repositorio.listar(Profesor.class);
    }

    // Busca por apellido (contiene, sin distinguir mayúsculas). Sin texto, devuelve todos.
    public List<Profesor> buscar(String texto) {
        return repositorio.buscar(Profesor.class, "apellido", texto);
    }

    public Profesor consultar(int id) {
        return repositorio.consultar(Profesor.class, id);
    }

    public void guardar(Profesor profesor) {
        normalizar(profesor);
        validar(profesor);

        // Un profesor nuevo todavía no tiene id (vale 0), así que no excluye a nadie.
        verificarDniLibre(profesor.getDni(), 0);

        repositorio.guardar(profesor);
    }

    public void actualizar(int id, Profesor datos) {
        Profesor existente = consultar(id);

        // Copiamos solo los datos del formulario: activo y cursos no se tocan.
        existente.setDni(datos.getDni());
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setEmail(datos.getEmail());
        existente.setTelefono(datos.getTelefono());

        normalizar(existente);
        validar(existente);

        // Excluimos al propio profesor: conservar su DNI no es un duplicado.
        verificarDniLibre(existente.getDni(), id);

        repositorio.guardar(existente);
    }

    // Baja lógica: el profesor queda inactivo pero conserva sus cursos.
    public void darDeBaja(int id) {
        Profesor profesor = consultar(id);
        profesor.setActivo(false);
        repositorio.guardar(profesor);
    }

    public void reactivar(int id) {
        Profesor profesor = consultar(id);
        profesor.setActivo(true);
        repositorio.guardar(profesor);
    }

    // Baja definitiva: solo si no tiene cursos. El repositorio no protege esta regla
    // (DataNucleus falla al borrar un profesor con cursos), por eso se valida acá.
    public void eliminar(int id) {
        Profesor profesor = consultar(id);

        if (!profesor.getCursos().isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar: el profesor tiene cursos asignados");
        }

        repositorio.eliminar(Profesor.class, id);
    }

    // El formulario manda "" en los campos vacíos: quitamos espacios sobrantes
    // y los campos vacíos quedan en null.
    private void normalizar(Profesor profesor) {
        profesor.setDni(vacioANull(profesor.getDni()));
        profesor.setNombre(vacioANull(profesor.getNombre()));
        profesor.setApellido(vacioANull(profesor.getApellido()));
        profesor.setEmail(vacioANull(profesor.getEmail()));
        profesor.setTelefono(vacioANull(profesor.getTelefono()));
    }

    // Se llama después de normalizar, así un campo con solo espacios ya es null.
    // Corta en el primer error: el mensaje nombra el campo que hay que corregir.
    private void validar(Profesor profesor) {
        obligatorio(profesor.getDni(), "El DNI es obligatorio");
        obligatorio(profesor.getNombre(), "El nombre es obligatorio");
        obligatorio(profesor.getApellido(), "El apellido es obligatorio");

        // Los largos coinciden con los de las columnas de Persona.
        largoMaximo(profesor.getDni(), 20, "El DNI no puede superar los 20 caracteres");
        largoMaximo(profesor.getNombre(), 100, "El nombre no puede superar los 100 caracteres");
        largoMaximo(profesor.getApellido(), 100, "El apellido no puede superar los 100 caracteres");
        largoMaximo(profesor.getEmail(), 100, "El email no puede superar los 100 caracteres");
        largoMaximo(profesor.getTelefono(), 20, "El teléfono no puede superar los 20 caracteres");

        // El email es opcional: solo se revisa si viene.
        if (profesor.getEmail() != null && !emailValido(profesor.getEmail())) {
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

    // buscar es "contiene", así que filtramos con equals para quedarnos con el DNI exacto.
    private void verificarDniLibre(String dni, int idExcluido) {
        boolean enUso = repositorio.buscar(Profesor.class, "dni", dni).stream()
                .anyMatch(otro -> otro.getDni().equals(dni) && otro.getId() != idExcluido);

        if (enUso) {
            throw new ReglaNegocioException("Ya existe un profesor con ese DNI");
        }
    }
}
