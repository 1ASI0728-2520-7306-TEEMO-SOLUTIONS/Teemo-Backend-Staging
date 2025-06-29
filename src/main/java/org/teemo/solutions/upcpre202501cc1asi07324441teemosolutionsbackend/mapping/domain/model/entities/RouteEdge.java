package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities;

import lombok.Getter;
// No necesita @Setter si la hacemos inmutable, lo cual es preferible.

// Ya no extiende AuditableAbstractAggregateRoot
@Getter
public class RouteEdge {
    private final Port source;
    private final Port destination;
    private final double distance;

    // Opcional: otros atributos como curvature y direction, si se usan.
    // private double curvature;
    // private String direction;

    // El constructor ahora es completo y correcto.
    public RouteEdge(Port source, Port destination, double distance) {
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Source and destination ports cannot be null.");
        }
        this.source = source;
        this.destination = destination;
        this.distance = distance;
    }
}