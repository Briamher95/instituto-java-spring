package com.programacion2.instituto.repositorio;

import java.time.LocalDate;

// Operación propia de las inscripciones: crearlas a partir de los ids,
// sin guardar objetos desprendidos de alumno o curso.
public interface IInscripcionRepositorio {

    void crear(int idAlumno, int idCurso, LocalDate fecha);
}
