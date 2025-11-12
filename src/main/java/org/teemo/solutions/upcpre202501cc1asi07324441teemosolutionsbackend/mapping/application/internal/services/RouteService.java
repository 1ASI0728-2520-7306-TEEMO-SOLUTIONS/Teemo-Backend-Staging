// Location: org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.NoViableRouteAvoidingDisabledPortsException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.RouteNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.GeoUtils;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.RouteCalculatorService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.SafetyValidator;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.mappers.PortMapper;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.CoordinatesResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteCalculationResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteRecalculationResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    private final RouteRepository routeRepository;
    private final PortRepository portRepository;
    private final RouteCalculatorService routeCalculatorService;
    private final SafetyValidator safetyValidator;
    private final GeoUtils geoUtils;
    private final PortMapper portMapper;

    public void saveAllRoutes(List<RouteDocument> routes) { routeRepository.saveAll(routes); }
    public boolean existsByHomePortAndDestinationPort(String h, String d) { return routeRepository.existsByHomePortAndDestinationPort(h, d); }
    public void deleteAllRoutes() { routeRepository.deleteAll(); }
    public List<RouteDocument> findAllRoutes() { return routeRepository.findAll(); }

    public RouteCalculationResource calculateOptimalRoute(String startPortId, String endPortId) {
        return calculateOptimalRoute(startPortId, endPortId, Collections.emptySet());
    }

    public RouteCalculationResource calculateOptimalRoute(String startPortId, String endPortId, Set<String> avoidPortIds) {
        Port startPort = findPortByIdOrThrow(startPortId);
        Port endPort = findPortByIdOrThrow(endPortId);
        return computeRoute(startPort, endPort, avoidPortIds).response();
    }

    public RouteRecalculationResource recalculateRouteAvoidingDisabledPorts(String routeId) {
        RouteDocument routeDocument = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Route not found: " + routeId));

        Port startPort = findPortByNameAndContinentOrThrow(routeDocument.getHomePort(), routeDocument.getHomePortContinent());
        Port endPort = findPortByNameAndContinentOrThrow(routeDocument.getDestinationPort(), routeDocument.getDestinationPortContinent());

        RouteComputationResult current = computeRoute(startPort, endPort, Collections.emptySet());

        List<String> disabledPortIds = current.ports().stream()
                .filter(Port::isDisabled)
                .map(Port::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (disabledPortIds.isEmpty()) {
            return new RouteRecalculationResource(routeId, current.response().optimalRoute(), false, List.of());
        }

        Set<String> avoidPortIds = new HashSet<>(disabledPortIds);

        if (avoidPortIds.contains(startPort.getId()) || avoidPortIds.contains(endPort.getId())) {
            throw new NoViableRouteAvoidingDisabledPortsException(
                    "Cannot recalculate route %s because one endpoint is disabled.".formatted(routeId),
                    avoidPortIds
            );
        }

        logger.info("route.recalculate routeId={} avoidedPortCount={}", routeId, avoidPortIds.size());

        try {
            RouteComputationResult recalculated = computeRoute(startPort, endPort, avoidPortIds);
            return new RouteRecalculationResource(
                    routeId,
                    recalculated.response().optimalRoute(),
                    true,
                    new ArrayList<>(avoidPortIds)
            );
        } catch (RouteNotFoundException ex) {
            throw new NoViableRouteAvoidingDisabledPortsException(
                    "No viable route for %s when avoiding %d disabled ports.".formatted(routeId, avoidPortIds.size()),
                    avoidPortIds
            );
        }
    }

    public double calculateTotalDistance(List<Port> route) {
        double total = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            Port currentPort = route.get(i);
            Port nextPort = route.get(i + 1);

            double segmentDistance = routeRepository.findByHomePortAndDestinationPort(currentPort.getName(), nextPort.getName())
                    .map(RouteDocument::getDistance)
                    .orElseGet(() -> {
                        logger.warn("Ruta no documentada entre {} y {}. Usando distancia Haversine como fallback.",
                                currentPort.getName(), nextPort.getName());
                        return geoUtils.calculateHaversineDistance(currentPort, nextPort);
                    });

            total += segmentDistance;
        }
        return total;
    }

    private Port findPortByIdOrThrow(String portId) {
        return portRepository.findById(portId)
                .map(portMapper::toDomain)
                .orElseThrow(() -> new PortNotFoundException("Puerto no encontrado: " + portId));
    }

    private Port findPortByNameAndContinentOrThrow(String name, String continent) {
        return portRepository.findByNameAndContinent(name, continent)
                .map(portMapper::toDomain)
                .orElseThrow(() -> new PortNotFoundException(
                        "Puerto no encontrado: %s (%s)".formatted(name, continent)));
    }

    private Map<String, CoordinatesResource> createCoordinatesMapping(List<Port> ports) {
        return ports.stream()
                .collect(Collectors.toMap(
                        Port::getName,
                        portMapper::toResource,
                        (existing, replacement) -> existing
                ));
    }

    private RouteComputationResult computeRoute(Port startPort, Port endPort, Set<String> avoidPortIds) {
        List<Port> optimalRoute = routeCalculatorService.calculateOptimalRoute(startPort, endPort, avoidPortIds);
        double totalDistance = calculateTotalDistance(optimalRoute);
        List<String> warnings = safetyValidator.validateFullRoute(optimalRoute);
        RouteCalculationResource response = new RouteCalculationResource(
                optimalRoute.stream().map(Port::getName).toList(),
                totalDistance,
                warnings,
                createCoordinatesMapping(optimalRoute)
        );
        return new RouteComputationResult(optimalRoute, response);
    }

    private record RouteComputationResult(List<Port> ports, RouteCalculationResource response) {}
}
