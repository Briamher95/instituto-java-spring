package com.programacion2.instituto.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.programacion2.instituto.dominio.Alumno;
import com.programacion2.instituto.repositorio.IRepositorio;

// Registra esta clase como un componente de la capa de servicios.
@Service
public class AlumnoServicio {

    private final IRepositorio<Alumno> repositorio;

    // Spring inyecta el repositorio por el único constructor, sin usar @Autowired.
    public AlumnoServicio(IRepositorio<Alumno> repositorio) {
        this.repositorio = repositorio;
    }

    public void guardar(Alumno alumno) {
        repositorio.guardar(alumno);
    }

    public List<Alumno> listar() {
        return repositorio.listar(Alumno.class);
    }
}
