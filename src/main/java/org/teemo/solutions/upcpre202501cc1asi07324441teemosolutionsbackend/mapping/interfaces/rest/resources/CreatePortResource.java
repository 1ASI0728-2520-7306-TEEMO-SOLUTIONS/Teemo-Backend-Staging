package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

public record CreatePortResource(
        String name,
        CoordinatesResource coordinates,
        String continent
) {}
