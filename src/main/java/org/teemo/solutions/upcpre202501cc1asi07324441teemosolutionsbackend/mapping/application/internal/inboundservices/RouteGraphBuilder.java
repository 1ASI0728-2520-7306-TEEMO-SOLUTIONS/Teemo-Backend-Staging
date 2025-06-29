package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.PortNotFoundException;
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
        portRepository.getAll().forEach(graph::addNode);
        routeRepository.getAll().forEach(routeDoc -> processRouteDocument(routeDoc, graph));
        return graph;
    }

    private void processRouteDocument(RouteDocument route, RouteGraph graph) {
        try {
            Port origin = getValidPort(route.getHomePort(), route.getHomeContinent());
            Port destination = getValidPort(route.getDestinationPort(), route.getDestinationContinent());

            // La distancia en el grafo será la distancia base.
            // Los ajustes se aplicarán dinámicamente en A*.
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
        // Crea las aristas una sola vez
        RouteEdge abEdge = new RouteEdge(a, b, baseDistance);
        RouteEdge baEdge = new RouteEdge(b, a, baseDistance);

        // Añade las aristas al grafo. El coste será calculado en tiempo de ejecución por A*
        graph.addEdge(a, abEdge);
        graph.addEdge(b, baEdge);
    }

}