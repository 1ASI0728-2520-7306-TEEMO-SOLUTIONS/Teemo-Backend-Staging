package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;

import java.util.*;

public class RouteGraph {
    private final Map<Port, List<RouteEdge>> adjacencyList = new HashMap<>();

    public boolean containsNode(Port port) {
        return adjacencyList.containsKey(port);
    }

    public List<RouteEdge> getAdjacentEdges(Port port) {
        return adjacencyList.getOrDefault(port, Collections.emptyList());
    }
    public void addEdge(Port source, RouteEdge edge) {
        adjacencyList.computeIfAbsent(source, k -> new ArrayList<>()).add(edge);
    }

    public int getNodeCount() {
        return adjacencyList.size();
    }

    public int getEdgeCount() {
        return adjacencyList.values().stream()
                .mapToInt(List::size)
                .sum();
    }
}