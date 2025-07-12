package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Route;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.RouteNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.GeoUtils;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteGraph;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.NavigationConditionsProvider;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.SafetyValidator;

import java.time.Clock;
import java.util.*;

@Component
public class AStarPathfinder {
    private static final Logger logger = LoggerFactory.getLogger(AStarPathfinder.class);

    // --- Constantes de Costes y Heurísticas ---
    private static final double SAFETY_PENALTY = 500.0;
    private static final double FAVORABLE_CURRENT_BONUS = 300.0;
    private static final double AGAINST_CURRENT_PENALTY = 200.0;
    private static final double HEURISTIC_SAME_CONTINENT_MULTIPLIER = 0.9;
    private static final double HEURISTIC_DIFFERENT_CONTINENT_PENALTY = 1.5;
    private static final double HEURISTIC_HEMISPHERE_CROSSING_PENALTY = 1.2;

    private final SafetyValidator safetyValidator;
    private final NavigationConditionsProvider navConditions;
    private final GeoUtils geoUtils;
    private final Clock clock;

    private record Node(Port port, double gScore, double hScore) {
        public double fScore() { return gScore + hScore; }
    }

    public AStarPathfinder(SafetyValidator safetyValidator,
                           NavigationConditionsProvider navConditions,
                           GeoUtils geoUtils,
                           Clock clock) {
        this.safetyValidator = safetyValidator;
        this.navConditions = navConditions;
        this.geoUtils = geoUtils;
        this.clock = clock;
    }

    public List<Port> findOptimalRoute(Port start, Port end, RouteGraph graph) {

        logger.info("Iniciando búsqueda de ruta desde el puerto: Nombre='{}', Continente='{}', HashCode={}",
                start.getName(), start.getContinent(), start.hashCode());

        validateInputs(start, end, graph);

        final Set<String> unsafePortNames = safetyValidator.getUnsafePortNames();
        logger.info("Búsqueda A* iniciada con {} puertos marcados como inseguros.", unsafePortNames.size());

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::fScore));
        Map<Port, Double> gScore = new HashMap<>();
        Map<Port, Port> cameFrom = new HashMap<>();

        initializeSearch(start, end, openSet, gScore);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.port().equals(end)) {
                return reconstructPath(cameFrom, current.port());
            }
            // Pasamos el conjunto de puertos inseguros para una verificación rápida.
            processNeighbors(current, end, graph, openSet, gScore, cameFrom, unsafePortNames);
        }

        logger.warn("No se pudo encontrar una ruta desde '{}' hasta '{}'", start.getName(), end.getName());
        throw new RouteNotFoundException(start.getName(), end.getName());
    }

    private void processNeighbors(Node current, Port end, RouteGraph graph,
                                  PriorityQueue<Node> openSet, Map<Port, Double> gScore,
                                  Map<Port, Port> cameFrom, Set<String> unsafePortNames) { // <-- Acepta el Set
        for (Route edge : graph.getAdjacentEdges(current.port())) {
            Port neighbor = edge.getDestinationPort();

            // Pasamos el set al método de cálculo de coste.
            double totalEdgeCost = calculateTotalEdgeCost(edge, unsafePortNames);

            updateNodeIfBetterPathFound(neighbor, current, totalEdgeCost, end, openSet, gScore, cameFrom);
        }
    }

    /**
     * Calcula el coste total de viajar a través de una ruta (edge).
     * La firma ahora es más simple gracias al nuevo modelo de Route.
     */
    private double calculateTotalEdgeCost(Route route, Set<String> unsafePortNames) {
        /*
        Port from = route.getHomePort();
        Port to = route.getDestinationPort();

        double adjustedBaseCost = navConditions.getAdjustedCost(route, from, to, clock);
        // La comprobación de seguridad ahora es súper rápida.
        double safetyPenalty = calculateSafetyFactor(from, to, unsafePortNames);
        double currentAdjustment = calculateCurrentAdjustment(from, to);

        double totalCost = adjustedBaseCost + safetyPenalty + currentAdjustment;

        return Math.max(0.0, totalCost);
        */

        return route.getDistance();

    }

    /**
     * Devuelve una penalización si la ruta es considerada insegura.
     * La llamada ahora usa un funcion más específico.
     */
    private double calculateSafetyFactor(Port a, Port b, Set<String> unsafePortNames) {
        // Comprobación en memoria, casi instantánea.
        if (unsafePortNames.contains(a.getName()) || unsafePortNames.contains(b.getName())) {
            return SAFETY_PENALTY;
        }
        return 0.0;
    }

    private void validateInputs(Port start, Port end, RouteGraph graph) {
        if (!graph.containsNode(start)) {
            throw new IllegalArgumentException("El puerto inicial '" + start.getName() + "' no existe en el grafo.");
        }
        if (!graph.containsNode(end)) {
            throw new IllegalArgumentException("El puerto final '" + end.getName() + "' no existe en el grafo.");
        }
    }

    private void initializeSearch(Port start, Port end, PriorityQueue<Node> openSet, Map<Port, Double> gScore) {
        gScore.put(start, 0.0);
        double hScore = calculateHeuristic(start, end);
        openSet.add(new Node(start, 0.0, hScore));
    }

    private void updateNodeIfBetterPathFound(Port neighbor, Node current, double edgeCost, Port end,
                                             PriorityQueue<Node> openSet, Map<Port, Double> gScore,
                                             Map<Port, Port> cameFrom) {
        double tentativeGScore = gScore.get(current.port()) + edgeCost;
        if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
            cameFrom.put(neighbor, current.port());
            gScore.put(neighbor, tentativeGScore);
            double hScore = calculateHeuristic(neighbor, end);
            openSet.add(new Node(neighbor, tentativeGScore, hScore));
        }
    }

    private double calculateHeuristic(Port current, Port target) {
        double baseDistance = geoUtils.calculateHaversineDistance(current, target);
        double continentFactor = current.getContinent().equals(target.getContinent()) ? HEURISTIC_SAME_CONTINENT_MULTIPLIER : HEURISTIC_DIFFERENT_CONTINENT_PENALTY;
        boolean sameHemisphere = current.getCoordinates().latitude() * target.getCoordinates().latitude() >= 0;
        double hemisphereFactor = sameHemisphere ? 1.0 : HEURISTIC_HEMISPHERE_CROSSING_PENALTY;
        return baseDistance * continentFactor * hemisphereFactor;
    }

    private double calculateCurrentAdjustment(Port a, Port b) {
        if (navConditions.isAgainstStrongCurrent(a, b)) return AGAINST_CURRENT_PENALTY;
        if (navConditions.isAlongFavorableCurrent(a, b)) return -FAVORABLE_CURRENT_BONUS;
        return 0.0;
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
}