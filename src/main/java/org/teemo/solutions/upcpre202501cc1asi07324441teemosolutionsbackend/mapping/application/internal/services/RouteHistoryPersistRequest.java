package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import lombok.Builder;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistorySource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;

import java.util.List;
import java.util.Map;

@Builder
public record RouteHistoryPersistRequest(
        String userId,
        String tenantId,
        String routeId,
        String originPortId,
        String originPortName,
        String destinationPortId,
        String destinationPortName,
        List<String> waypointPortIds,
        List<String> avoidedPortIds,
        Double totalDistance,
        Double durationEstimate,
        Double costEstimate,
        RouteHistoryStatus status,
        RouteHistorySource source,
        String notes,
        String engineVersion,
        String pathEncoding,
        Map<String, Object> geojson,
        Map<String, Object> metadata
) {}
