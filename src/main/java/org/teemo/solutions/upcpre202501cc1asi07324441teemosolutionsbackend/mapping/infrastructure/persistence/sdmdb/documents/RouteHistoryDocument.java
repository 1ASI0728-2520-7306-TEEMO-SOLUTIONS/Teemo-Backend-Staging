package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistorySource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "route_history")
@CompoundIndexes({
        @CompoundIndex(name = "idx_route_history_user_computedAt", def = "{'userId': 1, 'computedAt': -1}"),
        @CompoundIndex(name = "idx_route_history_tenant_user_computedAt", def = "{'tenantId': 1, 'userId': 1, 'computedAt': -1}"),
        @CompoundIndex(name = "idx_route_history_route_computedAt", def = "{'routeId': 1, 'computedAt': -1}"),
        @CompoundIndex(name = "idx_route_history_archived_user", def = "{'archived': 1, 'userId': 1}"),
        @CompoundIndex(name = "idx_route_history_dedupHash", def = "{'dedupHash': 1}")
})
public class RouteHistoryDocument {
    @Id
    private String id;
    @Indexed
    private String tenantId;
    @Indexed
    private String userId;
    private String routeId;
    private String originPortId;
    private String destinationPortId;
    private List<String> waypointPortIds;
    private List<String> avoidedPortIds;
    private Instant computedAt;
    private String engineVersion;
    private Double totalDistance;
    private Double durationEstimate;
    private Double costEstimate;
    private RouteHistoryStatus status;
    private RouteHistorySource source;
    private String notes;
    private String pathEncoding;
    private Map<String, Object> geojson;
    private String dedupHash;
    private boolean archived;
    private Map<String, Object> metadata;
}
