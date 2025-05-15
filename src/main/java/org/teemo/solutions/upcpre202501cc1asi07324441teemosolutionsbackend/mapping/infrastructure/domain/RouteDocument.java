package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Getter
@Setter
@Document(collection = "routes-documents")
@CompoundIndex(name = "unique_route", def = "{'homePort': 1, 'destinationPort': 1}", unique = true)
public class RouteDocument extends AuditableAbstractAggregateRoot<RouteDocument> {
    @Field("Home Port") // 👈 Mapear campo con espacio
    private String homePort;

    @Field("Destination Port")
    private String destinationPort;

    @Field("Distance")
    private Double distance; // Ej: 2000.0

    public RouteDocument(String homePort, String destinationPort, double distance) {
        this.homePort = homePort;
        this.destinationPort = destinationPort;
        this.distance = distance;
    }
}