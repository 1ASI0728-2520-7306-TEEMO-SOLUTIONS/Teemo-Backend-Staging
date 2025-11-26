package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.PortOperationalStatus;

import java.time.Instant;

public record PortOverviewItemResource(
        String portId,
        String name,
        String country,
        double lat,
        double lon,
        PortOperationalStatus status,
        String reason,
        int traffic,
        Instant updatedAt,
        String contactPhone,
        String contactEmail,
        String website
) {}
