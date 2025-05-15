package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Getter
@Setter
@Document(collection = "Rutas")
public class RouteDocument extends AuditableAbstractAggregateRoot<RouteDocument> {
    private String id;
    private String homePort;
    private String destinationPort;
    private Double distance;

    public RouteDocument(String tokyo, String busan, double v) {
        this.homePort = tokyo;
        this.destinationPort = busan;
        this.distance = v;
    }
    // Getters y setters
}