// AStarPathfinder.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.RouteNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.GeoUtils;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteGraph;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.NavigationConditionsProvider;

import java.util.*;

@Component
public class AStarPathfinder {
    private static final Logger logger = LoggerFactory.getLogger(AStarPathfinder.class);
    private static final double SAFETY_PENALTY = 500;
    private static final double FAVORABLE_CURRENT_BONUS = 300;
    private static final double AGAINST_CURRENT_PENALTY = 200;

    // --- NUEVAS CONSTANTES PARA LA HEURÍSTICA ---
    /**
     * Multiplicador para la heurística cuando los puertos están en el mismo continente.
     * Un valor < 1.0 favorece estas rutas.
     */
    private static final double HEURISTIC_SAME_CONTINENT_MULTIPLIER = 0.9;

    /**
     * Penalización para la heurística cuando los puertos están en continentes diferentes.
     * Un valor > 1.0 penaliza estas rutas.
     */
    private static final double HEURISTIC_DIFFERENT_CONTINENT_PENALTY = 1.5;

    /**
     * Penalización para la heurística cuando la ruta cruza el ecuador (cambia de hemisferio).
     * Un valor > 1.0 penaliza estas rutas.
     */
    private static final double HEURISTIC_HEMISPHERE_CROSSING_PENALTY = 1.2;

    private final SafetyValidator safetyValidator;
    private final NavigationConditionsProvider navConditions;
    private final GeoUtils geoUtils;

    public AStarPathfinder(SafetyValidator safetyValidator,
                           NavigationConditionsProvider navConditions,
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

        // Lanza la nueva excepción del dominio
        throw new RouteNotFoundException(start.getName(), end.getName());
    }

    private void validateInputs(Port start, Port end, RouteGraph graph) {
        if (graph.containsNode(start)) {
            logger.error("Puerto inicial no existe en el grafo: {}", start.getName());
            throw new RouteNotFoundException("Puerto inicial no tiene rutas: " + start.getName());
        }
        if (graph.containsNode(end)) {
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
            Port neighbor = edge.getDestination();

            // 1. Obtener coste base de factores ambientales (que ya incluye distancia)
            double baseCost = navConditions.getAdjustedCost(edge, current.port);

            // 2. Añadir la penalización de seguridad
            double safetyPenalty = calculateSafetyFactor(current.port, neighbor);

            // 3. Aplicar los efectos de las corrientes (bonus o penalización)
            double currentEffect = calculateCurrentEffects(current.port, neighbor);

            // 4. Calcular el coste total del segmento
            // Un bonus (valor positivo) se resta para reducir el coste. Una penalización (valor negativo) se suma.
            double totalEdgeCost = baseCost + safetyPenalty - currentEffect;

            // Es buena práctica asegurarse de que el coste de un segmento no sea negativo
            if (totalEdgeCost < 0) {
                totalEdgeCost = 0;
            }

            // 5. Usar el coste total para actualizar el nodo
            updateNode(neighbor, current, totalEdgeCost, end, openSet, gScore, cameFrom);
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

        // Penaliza rutas que cruzan continentes innecesariamente
        double continentFactor = current.getContinent().equals(target.getContinent())
                ? HEURISTIC_SAME_CONTINENT_MULTIPLIER   // <-- Usa la constante
                : HEURISTIC_DIFFERENT_CONTINENT_PENALTY;  // <-- Usa la constante

        // Penaliza rutas que cruzan el ecuador
        boolean sameHemisphere = current.getCoordinates().latitude() * target.getCoordinates().latitude() > 0;
        double hemisphereFactor = sameHemisphere
                ? 1.0
                : HEURISTIC_HEMISPHERE_CROSSING_PENALTY;  // <-- Usa la constante

        return baseDistance * continentFactor * hemisphereFactor;
    }

    private double calculateSafetyFactor(Port a, Port b) {
        // La lógica aquí es correcta: devuelve 0 o una penalización.
        return safetyValidator.validateRoute(List.of(a, b)).isEmpty() ? 0 : SAFETY_PENALTY;
    }

    // Ajusta este metodo para que las penalizaciones sean negativas
    private double calculateCurrentEffects(Port a, Port b) {
        // Si la corriente es favorable, devuelve el BONUS (un valor positivo)
        if (navConditions.isAlongFavorableCurrent(a, b)) {
            return FAVORABLE_CURRENT_BONUS;
        }
        // Si la corriente es en contra, devuelve la PENALIZACIÓN como un valor negativo
        if (navConditions.isAgainstStrongCurrent(a, b)) {
            return -AGAINST_CURRENT_PENALTY;
        }
        // Si no hay efecto, devuelve 0
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