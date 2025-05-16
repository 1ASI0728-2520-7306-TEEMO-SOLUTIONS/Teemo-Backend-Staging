package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.exceptionhandlers;

public class PortNotFoundException extends RuntimeException {
    public PortNotFoundException(String mensaje) {
        super(mensaje);
    }
}