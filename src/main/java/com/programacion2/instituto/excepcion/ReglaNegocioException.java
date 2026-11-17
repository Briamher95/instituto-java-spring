package com.programacion2.instituto.excepcion;

// Ej si se rompe por el dni duplicado

public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
