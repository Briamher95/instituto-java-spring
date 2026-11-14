package com.programacion2.instituto.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.programacion2.instituto.dominio.Alumno;
import com.programacion2.instituto.dominio.Inscripcion;
import com.programacion2.instituto.excepcion.ReglaNegocioException;
import com.programacion2.instituto.repositorio.IRepositorio;

// Registra esta clase como un componente de la capa de servicios.
// Acá viven las reglas de negocio de los alumnos.
@Service
public class AlumnoServicio {

    private final IRepositorio<Alumno> repositorio;

    // Se usa para borrar las inscripciones al dar de baja a un alumno.
    private final IRepositorio<Inscripcion> repositorioInscripciones;

    // Spring inyecta los repositorios por el único constructor, sin usar @Autowired.
    // Es el mismo bean genérico Repositorio, visto con dos tipos distintos.
    public AlumnoServicio(IRepositorio<Alumno> repositorio,
                          IRepositorio<Inscripcion> repositorioInscripciones) {
        this.repositorio = repositorio;
        this.repositorioInscripciones = repositorioInscripciones;
    }

    public List<Alumno> listar() {
        return repositorio.listar(Alumno.class);
    }

    // Busca por apellido (contiene, sin distinguir mayúsculas). Sin texto, devuelve todos.
    public List<Alumno> buscar(String texto) {
        return repositorio.buscar(Alumno.class, "apellido", texto);
    }

    public Alumno consultar(int id) {
        return repositorio.consultar(Alumno.class, id);
    }

    public void guardar(Alumno alumno) {
        alumno.normalizar();
        alumno.validar();

        // Un alumno nuevo todavía no tiene id (vale 0), así que no excluye a nadie.
        verificarDniLibre(alumno.getDni(), 0);

        repositorio.guardar(alumno);
    }

    public void actualizar(int id, Alumno datos) {
        Alumno existente = consultar(id);

        // Copiamos solo los datos del formulario: activo e inscripciones no se tocan.
        existente.setDni(datos.getDni());
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setEmail(datos.getEmail());
        existente.setTelefono(datos.getTelefono());

        existente.normalizar();
        existente.validar();

        // Excluimos al propio alumno: conservar su DNI no es un duplicado.
        verificarDniLibre(existente.getDni(), id);

        repositorio.guardar(existente);
    }

    // Baja lógica: el alumno queda inactivo y se borran sus inscripciones,
    // así libera el cupo en los cursos. Reactivarlo no las restaura.
    public void darDeBaja(int id) {
        Alumno alumno = consultar(id);

        for (Inscripcion inscripcion : alumno.getInscripciones()) {
            repositorioInscripciones.eliminar(Inscripcion.class, inscripcion.getId());
        }

        // Volvemos a consultar: la copia anterior todavía tiene en su lista
        // las inscripciones recién borradas, y no queremos guardarla así.
        alumno = consultar(id);
        alumno.setActivo(false);
        repositorio.guardar(alumno);
    }

    public void reactivar(int id) {
        Alumno alumno = consultar(id);
        alumno.setActivo(true);
        repositorio.guardar(alumno);
    }

    // Baja definitiva: solo si no tiene inscripciones. El repositorio no protege
    // esta regla (DataNucleus falla al borrar un alumno con inscripciones), por eso se valida acá.
    public void eliminar(int id) {
        Alumno alumno = consultar(id);

        if (!alumno.getInscripciones().isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar: el alumno tiene inscripciones");
        }

        repositorio.eliminar(Alumno.class, id);
    }

    // buscar es "contiene", así que filtramos con equals para quedarnos con el DNI exacto.
    // Solo mira la tabla de alumnos: un profesor con el mismo DNI no es un duplicado.
    private void verificarDniLibre(String dni, int idExcluido) {
        boolean enUso = repositorio.buscar(Alumno.class, "dni", dni).stream()
                .anyMatch(otro -> otro.getDni().equals(dni) && otro.getId() != idExcluido);

        if (enUso) {
            throw new ReglaNegocioException("Ya existe un alumno con ese DNI");
        }
    }
}
