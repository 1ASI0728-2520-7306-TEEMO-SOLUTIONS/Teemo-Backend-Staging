package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates.AStarPathfinder;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteGraph;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.RouteCalculatorService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteCalculatorServiceImpl implements RouteCalculatorService {

    private final AStarPathfinder pathfinder;
    private final RouteGraphBuilder graphBuilder;

    @Override
    public List<Port> calculateOptimalRoute(Port start, Port end) {
        RouteGraph graph = graphBuilder.buildDynamicRouteGraph();
        return pathfinder.findOptimalRoute(start, end, graph);
    }
}