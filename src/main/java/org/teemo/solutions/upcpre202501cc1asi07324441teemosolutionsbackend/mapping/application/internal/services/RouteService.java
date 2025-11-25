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
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistorySource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.RouteCalculatorService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.SafetyValidator;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.mappers.PortMapper;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.PortDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.CoordinatesResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteCalculationResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteRecalculationResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteHistoryContext;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteHistoryPersistRequest;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteHistoryService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    private final RouteHistoryService routeHistoryService;

    public void saveAllRoutes(List<RouteDocument> routes) { routeRepository.saveAll(routes); }
    public boolean existsByHomePortAndDestinationPort(String h, String d) { return routeRepository.existsByHomePortAndDestinationPort(h, d); }
    public void deleteAllRoutes() { routeRepository.deleteAll(); }
    public List<RouteDocument> findAllRoutes() { return routeRepository.findAll(); }

    public RouteCalculationResource calculateOptimalRoute(String startPortId, String endPortId) {
        return calculateOptimalRoute(startPortId, endPortId, Collections.emptySet(), null);
    }

    public RouteCalculationResource calculateOptimalRoute(String startPortId, String endPortId, RouteHistoryContext historyContext) {
        return calculateOptimalRoute(startPortId, endPortId, Collections.emptySet(), historyContext);
    }

    public RouteCalculationResource calculateOptimalRoute(String startPortId, String endPortId, Set<String> avoidPortIds) {
        return calculateOptimalRoute(startPortId, endPortId, avoidPortIds, null);
    }

    public RouteCalculationResource calculateOptimalRoute(String startPortId, String endPortId, Set<String> avoidPortIds,
                                                          RouteHistoryContext historyContext) {
        Port startPort = findPortByIdOrThrow(startPortId);
        Port endPort = findPortByIdOrThrow(endPortId);
        RouteComputationResult result = computeRoute(startPort, endPort, avoidPortIds, false);
        persistSuccessfulHistory(historyContext, startPort, endPort, result, historyContext != null ? historyContext.routeId() : null);
        return result.response();
    }

    public RouteRecalculationResource recalculateRouteAvoidingDisabledPorts(String routeId) {
        return recalculateRouteAvoidingDisabledPorts(routeId, null);
    }

    public RouteRecalculationResource recalculateRouteAvoidingDisabledPorts(String routeId, RouteHistoryContext historyContext) {
        RouteDocument routeDocument = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Route not found: " + routeId));

        Port startPort = findPortByNameAndContinentOrThrow(routeDocument.getHomePort(), routeDocument.getHomePortContinent());
        Port endPort = findPortByNameAndContinentOrThrow(routeDocument.getDestinationPort(), routeDocument.getDestinationPortContinent());

        RouteComputationResult current = computeRoute(startPort, endPort, Collections.emptySet(), true);

        List<String> disabledPortIds = current.ports().stream()
                .filter(Port::isDisabled)
                .map(Port::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (disabledPortIds.isEmpty()) {
            persistSuccessfulHistory(historyContext, startPort, endPort, current, routeId);
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
            RouteComputationResult recalculated = computeRoute(startPort, endPort, avoidPortIds, false);
            persistSuccessfulHistory(historyContext, startPort, endPort, recalculated, routeId);
            return new RouteRecalculationResource(
                    routeId,
                    recalculated.response().optimalRoute(),
                    true,
                    new ArrayList<>(avoidPortIds)
            );
        } catch (RouteNotFoundException ex) {
            persistNoViableHistory(historyContext, startPort, endPort, avoidPortIds, routeId, ex.getMessage());
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

    private void persistSuccessfulHistory(RouteHistoryContext context,
                                          Port startPort,
                                          Port endPort,
                                          RouteComputationResult computationResult,
                                          String routeId) {
        if (!shouldPersistHistory(context)) {
            return;
        }
        RouteCalculationResource response = computationResult.response();
        List<String> waypointIds = extractWaypointPortIds(computationResult.ports());
        List<String> avoidedIds = toSortedList(computationResult.effectiveAvoidPortIds());
        RouteHistoryPersistRequest request = RouteHistoryPersistRequest.builder()
                .userId(context.userId())
                .tenantId(context.tenantId())
                .routeId(routeId != null ? routeId : context.routeId())
                .originPortId(startPort.getId())
                .originPortName(startPort.getName())
                .destinationPortId(endPort.getId())
                .destinationPortName(endPort.getName())
                .waypointPortIds(waypointIds)
                .avoidedPortIds(avoidedIds)
                .totalDistance(response.totalDistance())
                .durationEstimate(context.durationEstimate())
                .costEstimate(context.costEstimate())
                .status(RouteHistoryStatus.SUCCESS)
                .source(context.source() != null ? context.source() : RouteHistorySource.MANUAL)
                .notes(context.notes())
                .engineVersion(context.engineVersion())
                .pathEncoding(context.pathEncoding())
                .geojson(context.geojson())
                .metadata(buildMetadata(context, computationResult.ports().size(), avoidedIds.size()))
                .build();
        routeHistoryService.save(request);
    }

    private void persistNoViableHistory(RouteHistoryContext context,
                                        Port startPort,
                                        Port endPort,
                                        Set<String> avoidPortIds,
                                        String routeId,
                                        String notes) {
        if (!shouldPersistHistory(context)) {
            return;
        }
        List<String> avoidedIds = toSortedList(avoidPortIds);
        RouteHistoryPersistRequest request = RouteHistoryPersistRequest.builder()
                .userId(context.userId())
                .tenantId(context.tenantId())
                .routeId(routeId != null ? routeId : context.routeId())
                .originPortId(startPort != null ? startPort.getId() : null)
                .originPortName(startPort != null ? startPort.getName() : null)
                .destinationPortId(endPort != null ? endPort.getId() : null)
                .destinationPortName(endPort != null ? endPort.getName() : null)
                .waypointPortIds(List.of())
                .avoidedPortIds(avoidedIds)
                .status(RouteHistoryStatus.NO_VIABLE_ROUTE)
                .source(context.source() != null ? context.source() : RouteHistorySource.MANUAL)
                .notes(notes != null ? notes : context.notes())
                .engineVersion(context.engineVersion())
                .pathEncoding(context.pathEncoding())
                .geojson(context.geojson())
                .metadata(buildMetadata(context, 0, avoidedIds.size()))
                .build();
        routeHistoryService.save(request);
    }

    private boolean shouldPersistHistory(RouteHistoryContext context) {
        return context != null && context.userId() != null;
    }

    private List<String> extractWaypointPortIds(List<Port> ports) {
        if (ports.size() <= 2) {
            return List.of();
        }
        return ports.subList(1, ports.size() - 1).stream()
                .map(Port::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> toSortedList(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream().sorted().toList();
    }

    private Map<String, Object> buildMetadata(RouteHistoryContext context, int portCount, int avoidedCount) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("portCount", portCount);
        metadata.put("avoidedPortCount", avoidedCount);
        if (context != null && context.metadata() != null) {
            metadata.putAll(context.metadata());
        }
        return metadata;
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

    private RouteComputationResult computeRoute(Port startPort, Port endPort, Set<String> avoidPortIds, boolean includeDisabledPorts) {
        Set<String> disabledPortIds = loadDisabledPortIds();
        validateEndpointsAvailability(startPort, endPort, disabledPortIds);

        Set<String> effectiveAvoidPortIds = new HashSet<>(avoidPortIds);
        if (!includeDisabledPorts) {
            effectiveAvoidPortIds.addAll(disabledPortIds);
        }

        List<Port> optimalRoute = routeCalculatorService.calculateOptimalRoute(startPort, endPort, effectiveAvoidPortIds);
        double totalDistance = calculateTotalDistance(optimalRoute);
        List<String> warnings = safetyValidator.validateFullRoute(optimalRoute);
        RouteCalculationResource response = new RouteCalculationResource(
                optimalRoute.stream().map(Port::getName).toList(),
                totalDistance,
                warnings,
                createCoordinatesMapping(optimalRoute)
        );
        return new RouteComputationResult(optimalRoute, response, Collections.unmodifiableSet(effectiveAvoidPortIds));
    }

    private Set<String> loadDisabledPortIds() {
        return portRepository.findByDisabled(true).stream()
                .map(PortDocument::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void validateEndpointsAvailability(Port startPort, Port endPort, Set<String> disabledPortIds) {
        if (startPort.getId() != null && disabledPortIds.contains(startPort.getId())) {
            throw new RouteNotFoundException("El puerto de origen '%s' esta deshabilitado temporalmente.".formatted(startPort.getName()));
        }
        if (endPort.getId() != null && disabledPortIds.contains(endPort.getId())) {
            throw new RouteNotFoundException("El puerto de destino '%s' esta deshabilitado temporalmente.".formatted(endPort.getName()));
        }
    }
    private record RouteComputationResult(List<Port> ports, RouteCalculationResource response, Set<String> effectiveAvoidPortIds) {}
}
