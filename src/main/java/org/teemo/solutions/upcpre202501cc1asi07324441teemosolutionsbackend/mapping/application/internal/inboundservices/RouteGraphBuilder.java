package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Route;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteGraph;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.mappers.PortMapper;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteRepository;

import java.util.Set;

@Component
public class RouteGraphBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RouteGraphBuilder.class);

    private final RouteRepository routeRepository;
    private final PortRepository portRepository;
    private final PortMapper portMapper;

    public RouteGraphBuilder(RouteRepository routeRepository,
                             PortRepository portRepository,
                             PortMapper portMapper) {
        this.routeRepository = routeRepository;
        this.portRepository = portRepository;
        this.portMapper = portMapper;
    }

    public RouteGraph buildDynamicRouteGraph(Set<String> avoidPortIds) {
        RouteGraph graph = new RouteGraph();
        routeRepository.findAll().forEach(route -> {
            try {
                processRouteDocument(route, graph, avoidPortIds);
            } catch (PortNotFoundException e) {
                logger.warn("Omission de ruta: {}", e.getMessage());
            }
        });
        return graph;
    }

    private void processRouteDocument(RouteDocument route, RouteGraph graph, Set<String> avoidPortIds) {
        try {
            Port origin = getValidPort(route.getHomePort(), route.getHomePortContinent());
            Port destination = getValidPort(route.getDestinationPort(), route.getDestinationPortContinent());
            if (isPortAvoided(origin, avoidPortIds) || isPortAvoided(destination, avoidPortIds)) {
                return;
            }
            addDynamicEdgePair(graph, origin, destination, route.getDistance());

            logger.info("Anadiendo al grafo: Puerto Origen='{}', Continente='{}', HashCode={}",
                    origin.getName(), origin.getContinent(), origin.hashCode());
            logger.info("Anadiendo al grafo: Puerto Destino='{}', Continente='{}', HashCode={}",
                    destination.getName(), destination.getContinent(), destination.hashCode());

        } catch (PortNotFoundException e) {
            logger.warn("Omission de ruta: {}", e.getMessage());
        }
    }

    private Port getValidPort(String name, String continent) {
        return portRepository.findByNameAndContinent(name, continent)
                .map(portMapper::toDomain)
                .orElseThrow(() -> new PortNotFoundException("Port not found: " + name + " in continent: " + continent));
    }

    private void addDynamicEdgePair(RouteGraph graph, Port a, Port b, double baseDistance) {
        Route route = new Route(a, b, baseDistance);
        graph.addEdge(route);
    }

    private boolean isPortAvoided(Port port, Set<String> avoidPortIds) {
        return port.getId() != null && avoidPortIds.contains(port.getId());
    }
}
