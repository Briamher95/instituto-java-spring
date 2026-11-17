package com.programacion2.instituto.repositorio;

import java.time.LocalDate;


public interface IInscripcionRepositorio {

    void crear(int idAlumno, int idCurso, LocalDate fecha);
}
