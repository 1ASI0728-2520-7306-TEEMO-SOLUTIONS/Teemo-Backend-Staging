package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources;

import java.util.List;

public record MapResource(
        Long id,
        String name,
        List<String> ports,
        List<String> edges
) {}
