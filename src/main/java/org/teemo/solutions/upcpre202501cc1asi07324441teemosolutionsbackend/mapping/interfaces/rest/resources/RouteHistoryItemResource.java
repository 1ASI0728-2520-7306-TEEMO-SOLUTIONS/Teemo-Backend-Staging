package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistorySource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record RouteHistoryItemResource(
        String id,
        String tenantId,
        String userId,
        String routeId,
        String originPortId,
        String originPortName,
        String destinationPortId,
        String destinationPortName,
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
        boolean archived,
        Map<String, Object> metadata,
        String pathEncoding,
        Map<String, Object> geojson
) {}
