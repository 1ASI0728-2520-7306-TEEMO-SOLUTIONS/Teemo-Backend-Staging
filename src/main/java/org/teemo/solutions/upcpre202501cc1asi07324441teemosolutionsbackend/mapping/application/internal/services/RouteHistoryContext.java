package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import lombok.Builder;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistorySource;

import java.util.Map;

@Builder
public record RouteHistoryContext(
        String userId,
        String tenantId,
        RouteHistorySource source,
        String routeId,
        String engineVersion,
        Double durationEstimate,
        Double costEstimate,
        String notes,
        String pathEncoding,
        Map<String, Object> geojson,
        Map<String, Object> metadata
) {}
