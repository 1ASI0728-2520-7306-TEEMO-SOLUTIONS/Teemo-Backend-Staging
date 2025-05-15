package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities;

import lombok.Getter;
import lombok.Setter;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Getter
@Setter
public class RouteEdge extends AuditableAbstractAggregateRoot<RouteEdge> {
    private Port source;
    private Port destination;
    private double distance;
    private double curvature;
    private String direction;

    public RouteEdge(Port destination, Double distance) {
        this.destination = destination;
        this.distance = distance;
    }
}
