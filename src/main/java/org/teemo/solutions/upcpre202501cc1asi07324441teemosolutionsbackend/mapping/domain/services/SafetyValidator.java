// SafetyValidator.java (Interfaz Refactorizada)
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import java.util.List;
import java.util.Set;

public interface SafetyValidator {

    /**
     * Valida una ruta completa y devuelve una lista de mensajes de advertencia.
     * Ideal para validar el resultado final.
     */
    List<String> validateFullRoute(List<Port> route);

    /**
     * Obtiene un conjunto de nombres de puertos que actualmente se consideran inseguros
     * debido a eventos geopolíticos válidos.
     * Este es el método clave para un rendimiento óptimo.
     * @return Un Set de nombres de puertos (String).
     */
    Set<String> getUnsafePortNames();
}