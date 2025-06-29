package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;

/**
 * Interfaz (Puerto en Arquitectura Hexagonal) que define el contrato
 * para obtener condiciones de navegación y factores ambientales.
 * El dominio depende de esta abstracción, no de una implementación concreta.
 */
public interface NavigationConditionsProvider {
    // La firma ahora devuelve un double, el coste ajustado
    double getAdjustedCost(RouteEdge edge, Port currentPort);
    boolean isAlongFavorableCurrent(Port a, Port b);
    boolean isAgainstStrongCurrent(Port a, Port b);
}