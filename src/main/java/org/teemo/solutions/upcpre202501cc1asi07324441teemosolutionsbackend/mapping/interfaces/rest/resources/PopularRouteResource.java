package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

public record PopularRouteResource(
        String routeId,
        String originPortId,
        String originPortName,
        String destinationPortId,
        String destinationPortName,
        long searchesCount
) {}
