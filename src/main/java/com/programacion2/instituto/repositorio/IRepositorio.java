package com.programacion2.instituto.repositorio;

import java.util.List;

public interface IRepositorio<T> {

    void guardar(T objeto);

    T consultar(Class<T> clase, int id);

    List<T> listar(Class<T> clase);
}
