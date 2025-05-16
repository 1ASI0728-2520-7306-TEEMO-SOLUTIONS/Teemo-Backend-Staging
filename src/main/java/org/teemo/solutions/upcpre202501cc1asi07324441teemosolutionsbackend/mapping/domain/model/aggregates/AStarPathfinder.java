// AStarPathfinder.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.exceptionhandlers.RouteNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices.NavigationConditionsService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.GeoUtils;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteGraph;

import java.util.*;

@Component
public class AStarPathfinder {
    private static final Logger logger = LoggerFactory.getLogger(AStarPathfinder.class);
    private static final double SAFETY_PENALTY = 500;
    private static final double FAVORABLE_CURRENT_BONUS = 300;
    private static final double AGAINST_CURRENT_PENALTY = 200;

    private final SafetyValidator safetyValidator;
    private final NavigationConditionsService navConditions;
    private final GeoUtils geoUtils;

    public AStarPathfinder(SafetyValidator safetyValidator,
                           NavigationConditionsService navConditions,
                           GeoUtils geoUtils) {
        this.safetyValidator = safetyValidator;
        this.navConditions = navConditions;
        this.geoUtils = geoUtils;
    }

    public List<Port> findOptimalRoute(Port start, Port end, RouteGraph graph) {
        validateInputs(start, end, graph);

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<Port, Double> gScore = new HashMap<>();
        Map<Port, Port> cameFrom = new HashMap<>();

        initializeSearch(start, end, openSet, gScore);


        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.port.equals(end)) return reconstructPath(cameFrom, current.port);

            processNeighbors(current, end, graph, openSet, gScore, cameFrom);
        }
        throw new RouteNotFoundException("No se encontró ruta entre %s y %s".formatted(start.getName(), end.getName()));
    }

    private void validateInputs(Port start, Port end, RouteGraph graph) {
        if (!graph.containsNode(start)) {
            logger.error("Puerto inicial no existe en el grafo: {}", start.getName());
            throw new RouteNotFoundException("Puerto inicial no tiene rutas: " + start.getName());
        }
        if (!graph.containsNode(end)) {
            logger.error("Puerto final no existe en el grafo: {}", end.getName());
            throw new RouteNotFoundException("Puerto final no tiene rutas: " + end.getName());
        }
    }

    private void initializeSearch(Port start, Port end, PriorityQueue<Node> openSet, Map<Port, Double> gScore) {
        gScore.put(start, 0.0);
        openSet.add(new Node(start, 0.0, calculateHeuristic(start, end)));
    }

    private void processNeighbors(Node current, Port end, RouteGraph graph,
                                  PriorityQueue<Node> openSet, Map<Port, Double> gScore,
                                  Map<Port, Port> cameFrom) {
        for (RouteEdge edge : graph.getAdjacentEdges(current.port)) {
            double adjustedDistance = navConditions.applyEnvironmentalFactors(edge, current.port);
            updateNode(edge.getDestination(), current, adjustedDistance, end, openSet, gScore, cameFrom);
        }
    }

    private void updateNode(Port neighbor, Node current, double edgeCost, Port end,
                            PriorityQueue<Node> openSet, Map<Port, Double> gScore,
                            Map<Port, Port> cameFrom) {
        double tentativeGScore = gScore.get(current.port) + edgeCost;

        if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
            cameFrom.put(neighbor, current.port);
            gScore.put(neighbor, tentativeGScore);
            openSet.add(new Node(neighbor, tentativeGScore,
                    tentativeGScore + calculateHeuristic(neighbor, end)));
        }
    }

    private double calculateHeuristic(Port current, Port target) {
        double baseDistance = geoUtils.calculateHaversineDistance(current, target);
        double safetyFactor = calculateSafetyFactor(current, target);
        double currentEffects = calculateCurrentEffects(current, target);

        return (baseDistance * 0.95) + safetyFactor - currentEffects;
    }

    private double calculateSafetyFactor(Port a, Port b) {
        return safetyValidator.validateRoute(List.of(a, b)).isEmpty() ? 0 : SAFETY_PENALTY;
    }

    private double calculateCurrentEffects(Port a, Port b) {
        if (navConditions.isAlongFavorableCurrent(a, b)) return FAVORABLE_CURRENT_BONUS;
        if (navConditions.isAgainstStrongCurrent(a, b)) return AGAINST_CURRENT_PENALTY;
        return 0;
    }

    private List<Port> reconstructPath(Map<Port, Port> cameFrom, Port end) {
        LinkedList<Port> path = new LinkedList<>();
        Port current = end;

        while (current != null) {
            path.addFirst(current);
            current = cameFrom.get(current);
        }
        return path;
    }

    private record Node(Port port, double gScore, double fScore) {}
}