package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;

import java.time.Instant;

public record RouteReportSummaryResource(
        String historyId,
        String shipmentId,
        String routeLabel,
        String origin,
        String destination,
        RouteHistoryStatus status,
        double progress,
        Instant departureTime,
        Instant estimatedArrivalTime,
        Double totalDistance,
        Double costEstimate
) {}
