package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Map;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources.MapResource;

import java.util.List;
import java.util.stream.Collectors;

public class MapResourceFromEntityAssembler {
    public static MapResource toResourceFromEntity(Map map) {
        List<String> portNames = map.getPorts().stream()
                .map(Port::getName)
                .collect(Collectors.toList());

        List<String> edgeNames = map.getEdges().stream()
                .map(edge -> edge.getOriginPort().getName() + " → " + edge.getDestinationPort().getName())
                .collect(Collectors.toList());

        return new MapResource(
                map.getId(),
                map.getName(),
                portNames,
                edgeNames
        );
    }
}
