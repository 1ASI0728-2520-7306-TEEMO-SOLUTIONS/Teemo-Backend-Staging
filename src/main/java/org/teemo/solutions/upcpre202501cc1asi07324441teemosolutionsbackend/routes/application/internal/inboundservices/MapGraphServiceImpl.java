package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.application.internal.inboundservices;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.entities.Edge;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services.MapGraphService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.infrastructure.persistence.jpa.repositories.PortRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Getter
public class MapGraphServiceImpl implements MapGraphService {

    private final Map<String, Port> ports = new HashMap<>();
    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();
    private final PortRepository portRepository;

    @Override
    public List<String> findOptimalRoute(String startName, String goalName) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
        Map<String, Double> gScores = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();
        Set<String> closedSet = new HashSet<>();

        gScores.put(startName, 0.0);
        openSet.add(new Node(startName, heuristic(startName, goalName)));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentName = current.getName();

            if (currentName.equals(goalName)) {
                return reconstructPath(cameFrom, currentName);
            }

            closedSet.add(currentName);

            for (Edge edge : adjacencyList.getOrDefault(currentName, new ArrayList<>())) {
                String neighbor = edge.getDestinationPort().getName();
                if (closedSet.contains(neighbor)) continue;

                double tentativeG = gScores.getOrDefault(currentName, Double.MAX_VALUE) + edge.getDistance();

                if (tentativeG < gScores.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, currentName);
                    gScores.put(neighbor, tentativeG);
                    double f = tentativeG + heuristic(neighbor, goalName);
                    openSet.add(new Node(neighbor, f));
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> reconstructPath(Map<String, String> cameFrom, String current) {
        List<String> path = new LinkedList<>();
        path.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }
        return path;
    }

    private double heuristic(String fromName, String toName) {
        Port from = ports.get(fromName);
        Port to = ports.get(toName);

        double lat1 = from.getLatitude(), lon1 = from.getLongitude();
        double lat2 = to.getLatitude(), lon2 = to.getLongitude();

        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @RequiredArgsConstructor
    @Getter
    private static class Node {
        private final String name;
        private final double f;
    }

    @Override
    public Port getPortByName(String name) {
        return ports.getOrDefault(name, null);
    }

    @Override
    public List<Port> getAllPorts() {
        return portRepository.findAll();
    }

}
