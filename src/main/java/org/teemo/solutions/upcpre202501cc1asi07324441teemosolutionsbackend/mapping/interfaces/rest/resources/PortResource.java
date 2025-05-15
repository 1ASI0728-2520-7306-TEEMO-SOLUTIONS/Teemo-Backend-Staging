package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

// PortResource.java
public record PortResource(
        String id,
        String name,
        CoordinatesResource coordinates,
        String continent
) {}