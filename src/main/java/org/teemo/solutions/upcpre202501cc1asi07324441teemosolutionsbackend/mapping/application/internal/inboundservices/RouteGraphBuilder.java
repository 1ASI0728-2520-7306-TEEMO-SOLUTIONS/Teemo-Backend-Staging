package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.exceptionhandlers.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteGraph;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.MongoPortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.MongoRouteRepository;

@Component
public class RouteGraphBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RouteGraphBuilder.class);

    private final MongoRouteRepository routeRepository;
    private final MongoPortRepository portRepository;
    private final NavigationConditionsService navConditions;

    public RouteGraphBuilder(MongoRouteRepository routeRepository,
                             MongoPortRepository portRepository,
                             NavigationConditionsService navConditions) {
        this.routeRepository = routeRepository;
        this.portRepository = portRepository;
        this.navConditions = navConditions;
    }

    // RouteGraphBuilder.java
    public RouteGraph buildDynamicRouteGraph() {
        RouteGraph graph = new RouteGraph();

        // 1. Añadir TODOS los puertos como nodos
        portRepository.getAll().forEach(graph::addNode);

        // 2. Procesar rutas existentes
        routeRepository.getAll().forEach(route -> {
            try {
                Port origin = portRepository.getPortByNameAndContinent(
                                route.getHomePort(), route.getHomeContinent())
                        .orElseThrow();

                Port destination = portRepository.getPortByNameAndContinent(
                                route.getDestinationPort(), route.getDestinationContinent())
                        .orElseThrow();

                // 3. Añadir conexión bidireccional
                graph.addEdge(origin, new RouteEdge(destination, route.getDistance()));
                graph.addEdge(destination, new RouteEdge(origin, route.getDistance()));

            } catch (Exception e) {
                logger.warn("Ruta omitida: {} -> {}: {}",
                        route.getHomePort(), route.getDestinationPort(), e.getMessage());
            }
        });

        return graph;
    }

    private void processRouteDocument(RouteDocument route, RouteGraph graph) {
        try {
            Port origin = getValidPort(route.getHomePort(), route.getHomeContinent());
            Port destination = getValidPort(route.getDestinationPort(), route.getDestinationContinent());

            addDynamicEdgePair(graph, origin, destination, route.getDistance());

        } catch (PortNotFoundException e) {
            logger.warn("Omisión de ruta: {}", e.getMessage());
        }
    }

    private Port getValidPort(String name, String continent) {
        return portRepository.getPortByNameAndContinent(name, continent)
                .orElseThrow(() -> new PortNotFoundException(
                        "Puerto no encontrado: %s (%s)".formatted(name, continent)));
    }

    private void addDynamicEdgePair(RouteGraph graph, Port a, Port b, double baseDistance) {
        RouteEdge abEdge = new RouteEdge(b, baseDistance);
        RouteEdge baEdge = new RouteEdge(a, baseDistance);

        graph.addEdge(a, applyEnvironmentalFactors(abEdge, a));
        graph.addEdge(b, applyEnvironmentalFactors(baEdge, b));
    }

    private RouteEdge applyEnvironmentalFactors(RouteEdge edge, Port origin) {
        double adjustedDistance = navConditions.applyEnvironmentalFactors(edge, origin);
        return new RouteEdge(edge.getDestination(), adjustedDistance);
    }

}