package com.programacion2.instituto.repositorio;

import java.util.List;

public interface IRepositorio<T> {

    void guardar(T objeto);

    T consultar(Class<T> clase, int id);

    List<T> listar(Class<T> clase);

    void eliminar(Class<T> clase, int id);

    // campo: nombre del atributo String a filtrar; lo elige el programador, nunca el usuario.
    List<T> buscar(Class<T> clase, String campo, String texto);
}
