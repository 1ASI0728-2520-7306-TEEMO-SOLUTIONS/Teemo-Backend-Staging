package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;

import java.util.*;

public class RouteGraph {
    private final Map<Port, List<RouteEdge>> adjacencyList = new HashMap<>();

    // Añadir método getter
    public Map<Port, List<RouteEdge>> getAdjacencyList() {
        return Collections.unmodifiableMap(adjacencyList);
    }


    public void addEdge(Port source, RouteEdge edge) {
        adjacencyList.computeIfAbsent(source, k -> new ArrayList<>()).add(edge);
    }

    public List<RouteEdge> getEdges(Port port) {
        return adjacencyList.getOrDefault(port, Collections.emptyList());
    }
}
