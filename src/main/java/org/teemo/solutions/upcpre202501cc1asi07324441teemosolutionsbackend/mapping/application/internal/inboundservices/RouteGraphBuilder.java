package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Route;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteGraph;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteRepository;

@Component
public class RouteGraphBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RouteGraphBuilder.class);

    private final RouteRepository routeRepository;
    private final PortRepository portRepository;
    private final NavigationConditionsService navConditions;

    public RouteGraphBuilder(RouteRepository _routeRepository,
                             PortRepository _portRepository,
                             NavigationConditionsService _navConditions) {
        this.routeRepository = _routeRepository;
        this.portRepository = _portRepository;
        this.navConditions = _navConditions;
    }

    public RouteGraph buildDynamicRouteGraph() {
        RouteGraph graph = new RouteGraph();
        // Carga las rutas desde la base de datos
        routeRepository.findAll().forEach(route -> {
            try {
                processRouteDocument(route, graph);
            } catch (PortNotFoundException e) {
                logger.warn("Omisión de ruta: {}", e.getMessage());
            }
        });
        return graph;
    }

    private void processRouteDocument(RouteDocument route, RouteGraph graph) {
        try {
            Port origin = getValidPort(route.getHomePort(), route.getHomePortContinent());
            Port destination = getValidPort(route.getDestinationPort(), route.getDestinationPortContinent());
            addDynamicEdgePair(graph, origin, destination, route.getDistance());

            logger.info("Añadiendo al grafo: Puerto Origen='{}', Continente='{}', HashCode={}",
                    origin.getName(), origin.getContinent(), origin.hashCode());
            logger.info("Añadiendo al grafo: Puerto Destino='{}', Continente='{}', HashCode={}",
                    destination.getName(), destination.getContinent(), destination.hashCode());

        } catch (PortNotFoundException e) {
            logger.warn("Omisión de ruta: {}", e.getMessage());
        }
    }

    private Port getValidPort(String name, String continent) {
        return portRepository.findByNameAndContinent(name, continent)
                .map(portDocument -> new Port(
                        portDocument.getName(),
                        new Coordinates(
                                portDocument.getCoordinates().getLatitude(),
                                portDocument.getCoordinates().getLongitude()
                        ),
                        portDocument.getContinent()
                ))
                .orElseThrow(() -> new PortNotFoundException("Port not found: " + name + " in continent: " + continent));
    }

    private void addDynamicEdgePair(RouteGraph graph, Port a, Port b, double baseDistance) {
        // Crea la ruta en UNA SOLA dirección.
        Route route = new Route(a, b, baseDistance);

        // La clase RouteGraph se encarga de hacerla bidireccional.
        // Solo se necesita esta llamada.
        graph.addEdge(route);
    }
}