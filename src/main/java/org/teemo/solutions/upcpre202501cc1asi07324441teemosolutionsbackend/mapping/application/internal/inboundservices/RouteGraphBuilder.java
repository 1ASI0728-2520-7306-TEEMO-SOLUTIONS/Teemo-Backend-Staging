package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.exceptionhandlers.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteGraph;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RouteGraphBuilder {

    private final RouteRepository routeRepository;
    private final PortRepository portRepository;
    private final RouteGraph graph = new RouteGraph();

    public RouteGraph buildGraph() {
        List<RouteDocument> routes = routeRepository.findAll();

        routes.forEach(route -> {
            Port source = portRepository.findByName(route.getHomePort())
                    .orElseThrow(() -> new PortNotFoundException(route.getHomePort()));

            Port destination = portRepository.findByName(route.getDestinationPort())
                    .orElseThrow(() -> new PortNotFoundException(route.getDestinationPort()));

            // Crear arista bidireccional
            graph.addEdge(source, new RouteEdge(destination, route.getDistance()));
            graph.addEdge(destination, new RouteEdge(source, route.getDistance()));
        });

        return graph;
    }
}
