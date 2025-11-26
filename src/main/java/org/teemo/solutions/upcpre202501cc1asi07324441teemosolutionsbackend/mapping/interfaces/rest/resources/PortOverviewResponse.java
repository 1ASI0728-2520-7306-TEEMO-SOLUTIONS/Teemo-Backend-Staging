package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

public record PortOverviewResponse(
        List<PortOverviewItemResource> content,
        long totalElements,
        Instant lastSyncedAt
) {}
