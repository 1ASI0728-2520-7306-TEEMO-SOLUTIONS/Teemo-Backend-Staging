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

import java.util.List;

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

        // 1. Añadir TODOS los puertos como nodos (incluso sin rutas)
        List<Port> allPorts = portRepository.getAll();
        allPorts.forEach(graph::addNode); // Nuevo metodo addNode

        // 2. Procesar rutas para añadir aristas
        List<RouteDocument> routes = routeRepository.getAll();
        routes.forEach(route -> processRouteDocument(route, graph));

        logGraphStatistics(graph);
        return graph;
    }

    private void processRouteDocument(RouteDocument route, RouteGraph graph) {
        try {
            Port origin = getValidPort(route.getHomePort(), route.getHomeContinent());
            Port destination = getValidPort(route.getDestinationPort(), route.getDestinationContinent());

            addDynamicEdgePair(graph, origin, destination, route.getDistance());
            logRouteAddition(origin, destination, route.getDistance());

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

    private void logRouteAddition(Port origin, Port destination, double distance) {
        logger.debug("Conexión añadida: {} [{}] ↔ {} [{}] | Distancia base: {} mn",
                origin.getName(), origin.getContinent(),
                destination.getName(), destination.getContinent(),
                distance);
    }

    private void logGraphStatistics(RouteGraph graph) {
        logger.info("Grafo construido - Nodos: {}, Aristas: {}",
                graph.getNodeCount(), graph.getEdgeCount());
    }
}