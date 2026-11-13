package com.programacion2.instituto.excepcion;

// Indica que se rompió una regla de negocio (por ejemplo, un DNI repetido).
// El mensaje está pensado para mostrárselo al usuario tal cual.
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
