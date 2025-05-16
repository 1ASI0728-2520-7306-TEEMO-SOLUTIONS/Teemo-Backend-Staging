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
    @Field("Home Port")
    private String homePort;

    @Field("Home Continent")
    private String homeContinent;

    @Field("Destination Port")
    private String destinationPort;

    @Field("Destination Continent")
    private String destinationContinent;

    @Field("Distance")
    private Double distance;

    public RouteDocument(String homePort, String homeContinent, String destinationPort, String destinationContinent, Double distance) {
        this.homePort = homePort;
        this.homeContinent = homeContinent;
        this.destinationPort = destinationPort;
        this.destinationContinent = destinationContinent;
        this.distance = distance;
    }
}