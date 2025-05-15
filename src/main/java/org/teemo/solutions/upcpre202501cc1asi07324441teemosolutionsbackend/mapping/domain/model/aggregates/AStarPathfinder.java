package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates;

import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.exceptionhandlers.NoRouteFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteGraph;

import java.util.*;

@Component
public class AStarPathfinder {

    public List<Port> findPath(Port start, Port end, RouteGraph graph) {
        Map<Port, List<RouteEdge>> adjacencyList = graph.getAdjacencyList();
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<Port, Double> gScore = new HashMap<>();
        Map<Port, Port> cameFrom = new HashMap<>();

        gScore.put(start, 0.0);
        openSet.add(new Node(start, 0.0, heuristic(start, end)));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.port.equals(end)) {
                return reconstructPath(cameFrom, current.port);
            }

            for (RouteEdge edge : adjacencyList.getOrDefault(current.port, Collections.emptyList())) {
                double tentativeGScore = gScore.get(current.port) + edge.getDistance();

                if (tentativeGScore < gScore.getOrDefault(edge.getDestination(), Double.POSITIVE_INFINITY)) {
                    cameFrom.put(edge.getDestination(), current.port);
                    gScore.put(edge.getDestination(), tentativeGScore);
                    double fScore = tentativeGScore + heuristic(edge.getDestination(), end);
                    openSet.add(new Node(edge.getDestination(), tentativeGScore, fScore));
                }
            }
        }
        throw new NoRouteFoundException("No se encontró ruta entre " + start.getName() + " y " + end.getName());
    }

    private List<Port> reconstructPath(Map<Port, Port> cameFrom, Port current) {
        List<Port> path = new ArrayList<>();
        path.add(current);

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }
        return path;
    }

    private double heuristic(Port a, Port b) {
        double R = 6371; // Radio terrestre en km
        double lat1 = Math.toRadians(a.getCoordinates().latitude());
        double lon1 = Math.toRadians(a.getCoordinates().longitude());
        double lat2 = Math.toRadians(b.getCoordinates().latitude());
        double lon2 = Math.toRadians(b.getCoordinates().longitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double aFormula = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);

        return R * 2 * Math.atan2(Math.sqrt(aFormula), Math.sqrt(1 - aFormula));
    }

    private record Node(Port port, double gScore, double fScore) {
    }
}