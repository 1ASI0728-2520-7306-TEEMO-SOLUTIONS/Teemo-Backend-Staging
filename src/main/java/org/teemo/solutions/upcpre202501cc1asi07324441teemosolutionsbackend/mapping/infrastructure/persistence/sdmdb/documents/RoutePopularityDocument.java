package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "routes_popularity")
@CompoundIndexes({
        @CompoundIndex(name = "origin_destination_idx", def = "{'originPortId': 1, 'destinationPortId': 1}", unique = true)
})
public class RoutePopularityDocument {
    @Id
    private String id;
    private String routeId;
    private String originPortId;
    private String originPortName;
    private String destinationPortId;
    private String destinationPortName;
    private long searchesCount;
    private Instant createdAt;
    private Instant lastSearchedAt;
}
