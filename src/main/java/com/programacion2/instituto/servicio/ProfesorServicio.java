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
        profesor.normalizar();
        profesor.validar();

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

        existente.normalizar();
        existente.validar();

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

    // buscar es "contiene", así que filtramos con equals para quedarnos con el DNI exacto.
    private void verificarDniLibre(String dni, int idExcluido) {
        boolean enUso = repositorio.buscar(Profesor.class, "dni", dni).stream()
                .anyMatch(otro -> otro.getDni().equals(dni) && otro.getId() != idExcluido);

        if (enUso) {
            throw new ReglaNegocioException("Ya existe un profesor con ese DNI");
        }
    }
}
