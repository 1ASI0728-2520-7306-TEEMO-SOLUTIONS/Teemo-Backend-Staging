package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.exceptionhandlers.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices.RouteGraphBuilder;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates.AStarPathfinder;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates.SafetyValidator;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteGraph;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.CoordinatesResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteCalculationResource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final PortRepository portRepository;
    private final AStarPathfinder pathfinder;
    private final RouteGraphBuilder graphBuilder;
    private final SafetyValidator safetyValidator;

    public RouteCalculationResource calculateOptimalRoute(String startPortName, String endPortName) {
        Port start = portRepository.findByName(startPortName)
                .orElseThrow(() -> new PortNotFoundException(startPortName));

        Port end = portRepository.findByName(endPortName)
                .orElseThrow(() -> new PortNotFoundException(endPortName));

        RouteGraph graph = graphBuilder.buildGraph();
        List<Port> optimalRoute = pathfinder.findPath(start, end, graph);

        double totalDistance = calculateTotalDistance(optimalRoute, graph);
        List<String> warnings = safetyValidator.validateRoute(optimalRoute);

        return new RouteCalculationResource(
                optimalRoute.stream().map(Port::getName).toList(),
                totalDistance,
                warnings,
                createCoordinatesMapping(optimalRoute) // Mapeo correcto de coordenadas
        );
    }
    public Page<RouteDocument> getAllRoutes(Pageable pageable) {
        return routeRepository.findAll(pageable);
    }

    public Optional<Double> getDistanceBetweenPorts(String start, String end) {
        return routeRepository.findBetweenPorts(start, end)
                .map(RouteDocument::getDistance);
    }

    private double calculateTotalDistance(List<Port> route, RouteGraph graph) {
        double total = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            Port current = route.get(i);
            Port next = route.get(i + 1);
            total += graph.getAdjacencyList().get(current).stream()
                    .filter(edge -> edge.getDestination().equals(next))
                    .findFirst()
                    .map(RouteEdge::getDistance)
                    .orElse(0.0);
        }
        return total;
    }

    private Map<String, CoordinatesResource> createCoordinatesMapping(List<Port> ports) {
        return ports.stream()
                .collect(Collectors.toMap(
                        Port::getName,
                        port -> new CoordinatesResource(
                                port.getCoordinates().latitude(),
                                port.getCoordinates().longitude()
                        )));
    }
}