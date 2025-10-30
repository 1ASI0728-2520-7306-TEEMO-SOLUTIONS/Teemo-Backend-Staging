package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

public record RouteRequestResource(
        String startPort,
        String endPort,
        String continent // Nuevo campo
) {}