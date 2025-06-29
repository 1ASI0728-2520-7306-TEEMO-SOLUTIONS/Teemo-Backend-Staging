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

    // Nombres de campo corregidos a camelCase
    @Field("homePort")
    private String homePort;

    @Field("homeContinent")
    private String homeContinent;

    @Field("destinationPort")
    private String destinationPort;

    @Field("destinationContinent")
    private String destinationContinent;

    @Field("distance")
    private Double distance;

    // Actualiza el constructor para que coincida
    public RouteDocument(String homePort, String homeContinent, String destinationPort, String destinationContinent, Double distance) {
        this.homePort = homePort;
        this.homeContinent = homeContinent;
        this.destinationPort = destinationPort;
        this.destinationContinent = destinationContinent;
        this.distance = distance;
    }

    // Si los campos en Java coinciden con los de la BD, la anotación @Field es opcional.
    // Podrías incluso eliminarla para mayor limpieza. Por ejemplo:
    // private String homePort; // Spring Data lo mapeará a "homePort" por defecto.
}