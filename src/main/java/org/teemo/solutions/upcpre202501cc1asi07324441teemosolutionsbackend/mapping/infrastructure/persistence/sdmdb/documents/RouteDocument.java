package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "routes")
public class RouteDocument {
    @Id
    private String id;
    private String homePort;
    private String homePortContinent;
    private String destinationPort;
    private String destinationPortContinent;
    private Double distance;

    public RouteDocument() {}

    public RouteDocument(String homePort, String homePortContinent, String destinationPort, String destinationPortContinent, Double distance) {
        this.id = null; // MongoDB will generate an ID
        this.homePort = homePort;
        this.destinationPort = destinationPort;
        this.homePortContinent = homePortContinent;
        this.destinationPortContinent = destinationPortContinent;
        this.distance = distance;
    }

}