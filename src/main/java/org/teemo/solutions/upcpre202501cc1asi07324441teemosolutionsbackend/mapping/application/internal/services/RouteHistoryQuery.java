package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistorySource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;

import java.time.Instant;

public record RouteHistoryQuery(
        String tenantId,
        String userId,
        Instant from,
        Instant to,
        RouteHistoryStatus status,
        RouteHistorySource source,
        Boolean archived,
        String routeId
) {}
