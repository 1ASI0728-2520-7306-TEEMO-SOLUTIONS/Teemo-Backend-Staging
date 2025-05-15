package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.dtos;

import java.util.List;
import java.util.Map;

public record RouteResponse(
        List<PortDto> route,
        double totalDistance,
        List<String> warnings,
        Map<String, Double> coordinatesMapping // Para el frontend
) {}