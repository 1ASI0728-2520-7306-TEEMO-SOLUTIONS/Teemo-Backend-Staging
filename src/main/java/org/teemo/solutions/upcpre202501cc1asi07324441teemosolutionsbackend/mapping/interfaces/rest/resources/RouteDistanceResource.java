package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

import java.util.List;
import java.util.Map;

// RouteDistanceResource.java
public record RouteDistanceResource(
        Double distance,
        List<String> messages,
        Map<String, Object> meta
) {}
