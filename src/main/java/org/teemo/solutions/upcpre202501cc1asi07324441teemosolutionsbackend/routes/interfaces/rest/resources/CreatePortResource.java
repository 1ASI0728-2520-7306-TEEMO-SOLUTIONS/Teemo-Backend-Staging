package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources;

public record CreatePortResource(
        String name,
        Double latitude,
        Double longitude,
        String continent
) {}
