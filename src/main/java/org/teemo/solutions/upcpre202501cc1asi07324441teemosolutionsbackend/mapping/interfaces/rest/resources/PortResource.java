package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

import java.time.Instant;

// PortResource.java
public record PortResource(
        String id,
        String name,
        CoordinatesResource coordinates,
        String continent,
        boolean disabled,
        String disabledReason,
        Instant disabledAt,
        String disabledBy
) {}
