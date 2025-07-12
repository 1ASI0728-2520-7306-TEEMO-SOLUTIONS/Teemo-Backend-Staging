package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Route;

import java.util.*;

/**
 * Representa el mapa de rutas como un grafo no dirigido.
 * Los nodos son los nombres de los puertos (String) y las aristas son los objetos Route.
 * La estructura interna es una lista de adyacencia.
 */
public class RouteGraph {
    private static final Logger logger = LoggerFactory.getLogger(RouteGraph.class);
    private final Map<Port, List<Route>> adjacencyList = new HashMap<>();

    /**
     * Añade un nuevo puerto (nodo) al grafo si aún no existe.
     * @param portName El nombre del puerto a añadir.
     */
    public void addNode(Port portName) {
        adjacencyList.putIfAbsent(portName, new ArrayList<>());
    }

    /**
     * Añade una arista (ruta) al grafo de forma bidireccional.
     * Si se añade una ruta de A a B, también se añade la ruta inversa de B a A.
     * Esto es crucial para que los algoritmos de búsqueda de caminos funcionen correctamente.
     * @param route La ruta a añadir.
     */
    public void addEdge(Route route) {
        // Asegurarse de que ambos puertos (nodos) existen en el grafo
        addNode(route.getHomePort());
        addNode(route.getDestinationPort());

        // 1. Añadir la arista en la dirección original (A -> B)
        adjacencyList.get(route.getHomePort()).add(route);

        // 2. Crear la arista inversa (B -> A)
        Route reverseRoute = new Route(
                route.getDestinationPort(),
                route.getHomePort(),
                route.getDistance()
        );

        // 3. Añadir la arista inversa al grafo
        adjacencyList.get(reverseRoute.getHomePort()).add(reverseRoute);
    }

    /**
     * Devuelve todas las rutas (aristas) que salen de un puerto específico.
     * Gracias a que addEdge es bidireccional, esto devolverá todas las conexiones de un puerto.
     * @param portName El nombre del puerto (nodo).
     * @return Una lista de Rutas. Devuelve una lista vacía si el puerto no tiene conexiones o no existe.
     */
    public List<Route> getAdjacentEdges(Port portName) {
        return adjacencyList.getOrDefault(portName, Collections.emptyList());
    }

    /**
     * Comprueba si un puerto (nodo) existe en el grafo.
     * @param portName El nombre del puerto a buscar.
     * @return true si el puerto existe en el grafo, false en caso contrario.
     */
    public boolean containsNode(Port portName) {
        return adjacencyList.containsKey(portName);
    }

    /**
     * Devuelve el número total de puertos (nodos) en el grafo.
     * @return El conteo de nodos.
     */
    public int getNodeCount() {
        return adjacencyList.size();
    }

    public void logAllNodes() {
        logger.info("--- Estado Final de Nodos en RouteGraph ---");
        if (adjacencyList.isEmpty()) {
            logger.info("El grafo está vacío.");
            return;
        }

        adjacencyList.keySet().forEach(port ->
                logger.info("Nodo en grafo: Nombre='{}', Continente='{}', HashCode={}",
                        port.getName(), port.getContinent(), port.hashCode())
        );
        logger.info("--- Fin del Estado del Grafo ---");
    }

    /**
     * Devuelve el número total de conexiones únicas (aristas) en el grafo.
     * @return El conteo de aristas.
     */
    public int getEdgeCount() {
        // Sumamos todas las aristas y dividimos por 2 porque cada conexión (A-B)
        // se almacena dos veces (una como A->B y otra como B->A).
        int totalDirectedEdges = adjacencyList.values().stream()
                .mapToInt(List::size)
                .sum();
        return totalDirectedEdges / 2;
    }

    /**
     * Devuelve el conjunto de todos los nombres de puertos (nodos) en el grafo.
     * @return Un Set<String> con todos los nombres de los nodos.
     */
    public Set<Port> getAllNodes() {
        return adjacencyList.keySet();
    }
}