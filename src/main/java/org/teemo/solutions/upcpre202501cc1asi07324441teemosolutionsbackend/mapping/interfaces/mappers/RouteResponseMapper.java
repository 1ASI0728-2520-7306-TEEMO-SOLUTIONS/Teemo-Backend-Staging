package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.dtos.RouteResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RouteResponseMapper {

    private final PortMapper portMapper;

    public RouteResponse toResponse(List<Port> route, double distance, List<String> warnings) {
        return new RouteResponse(
                route.stream().map(portMapper::toDto).toList(),
                distance,
                warnings,
                calculateCoordinates(route)
        );
    }

    private Map<String, Double> calculateCoordinates(List<Port> route) {
        Map<String, Double> coordinates = new LinkedHashMap<>();
        route.forEach(port -> {
            coordinates.put(port.getName() + "_lat", port.getCoordinates().latitude());
            coordinates.put(port.getName() + "_lon", port.getCoordinates().longitude());
        });
        return coordinates;
    }
}