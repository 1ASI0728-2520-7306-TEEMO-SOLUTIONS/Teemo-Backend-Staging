package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions;

/**
 * Excepción de dominio que se lanza cuando no se puede encontrar una ruta
 * entre dos puertos. Representa un fallo en una regla de negocio fundamental.
 */
public class RouteNotFoundException extends RuntimeException {
    public RouteNotFoundException(String startPortName, String endPortName) {
        super("No se encontró una ruta válida entre el puerto '%s' y '%s'".formatted(startPortName, endPortName));
    }

    public RouteNotFoundException(String message) {
        super(message);
    }
}