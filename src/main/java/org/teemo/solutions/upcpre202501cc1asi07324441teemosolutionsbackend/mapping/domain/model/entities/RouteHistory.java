package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistorySource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Domain view of a persisted route history entry.
 */
public record RouteHistory(
        String id,
        String tenantId,
        String userId,
        String routeId,
        String originPortId,
        String destinationPortId,
        List<String> waypointPortIds,
        List<String> avoidedPortIds,
        Instant computedAt,
        String engineVersion,
        Double totalDistance,
        Double durationEstimate,
        Double costEstimate,
        RouteHistoryStatus status,
        RouteHistorySource source,
        String notes,
        String pathEncoding,
        Map<String, Object> geojson,
        String dedupHash,
        boolean archived,
        Map<String, Object> metadata) {

    public RouteHistory {
        waypointPortIds = List.copyOf(Objects.requireNonNullElse(waypointPortIds, List.of()));
        avoidedPortIds = List.copyOf(Objects.requireNonNullElse(avoidedPortIds, List.of()));
        metadata = Map.copyOf(Objects.requireNonNullElse(metadata, Map.of()));
    }
}
