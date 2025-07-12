// Ubicación: org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.GeoUtils;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.RouteCalculatorService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.SafetyValidator;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.mappers.PortMapper;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.CoordinatesResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteCalculationResource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok para inyección de dependencias en el constructor
public class RouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    // --- Inyección de dependencias (final y gestionado por Lombok) ---
    private final RouteRepository routeRepository;
    private final PortRepository portRepository;
    private final RouteCalculatorService routeCalculatorService;
    private final SafetyValidator safetyValidator; // <-- Inyectamos la interfaz
    private final GeoUtils geoUtils;             // <-- Inyectamos GeoUtils como un bean
    private final PortMapper portMapper;         // <-- Inyectamos nuestro nuevo mapper

    // --- Métodos de delegación simple al repositorio ---
    public void saveAllRoutes(List<RouteDocument> routes) { routeRepository.saveAll(routes); }
    public boolean existsByHomePortAndDestinationPort(String h, String d) { return routeRepository.existsByHomePortAndDestinationPort(h, d); }
    public void deleteAllRoutes() { routeRepository.deleteAll(); }
    public List<RouteDocument> findAllRoutes() { return routeRepository.findAll(); }

    /**
     * Orquesta el cálculo de la ruta óptima, su validación y la construcción de la respuesta.
     */
    public RouteCalculationResource calculateOptimalRoute(String startPortName, String endPortName) {
        // 1. Obtener y mapear los puertos de dominio usando el mapper. El código es más limpio.
        Port startPort = findPortOrThrow(startPortName);
        Port endPort = findPortOrThrow(endPortName);

        // 2. Calcular la ruta óptima a través del servicio de dominio.
        List<Port> optimalRoute = routeCalculatorService.calculateOptimalRoute(startPort, endPort);

        // 3. Calcular la distancia total de la ruta.
        double totalDistance = calculateTotalDistance(optimalRoute);

        // 4. Validar la seguridad de la ruta.
        List<String> warnings = safetyValidator.validateFullRoute(optimalRoute);

        // 5. Construir y devolver el recurso de respuesta.
        return new RouteCalculationResource(
                optimalRoute.stream().map(Port::getName).toList(),
                totalDistance,
                warnings,
                createCoordinatesMapping(optimalRoute)
        );
    }

    /**
     * Calcula la distancia total de una ruta, priorizando distancias documentadas
     * y usando Haversine como fallback de forma clara y segura.
     */
    public double calculateTotalDistance(List<Port> route) {
        double total = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            Port currentPort = route.get(i);
            Port nextPort = route.get(i + 1);

            // Lógica de "encuentra la ruta o calcula la distancia" simplificada con Optional
            double segmentDistance = routeRepository.findByHomePortAndDestinationPort(currentPort.getName(), nextPort.getName())
                    .map(RouteDocument::getDistance) // Si la ruta existe, toma su distancia
                    .orElseGet(() -> { // Si no, calcula la distancia Haversine
                        logger.warn("Ruta no documentada entre {} y {}. Usando distancia Haversine como fallback.",
                                currentPort.getName(), nextPort.getName());
                        return geoUtils.calculateHaversineDistance(currentPort, nextPort);
                    });

            total += segmentDistance;
        }
        return total;
    }

    /**
     * Busca un puerto por su nombre y lo mapea a la entidad de dominio, o lanza una excepción.
     */
    private Port findPortOrThrow(String portName) {
        return portRepository.findByName(portName)
                .map(portMapper::toDomain) // Usa la referencia al funcion del mapper
                .orElseThrow(() -> new PortNotFoundException("Puerto no encontrado: " + portName));
    }

    /**
     * Crea un mapa de nombres de puertos a sus coordenadas usando el mapper.
     */
    private Map<String, CoordinatesResource> createCoordinatesMapping(List<Port> ports) {
        return ports.stream()
                .collect(Collectors.toMap(
                        Port::getName,
                        portMapper::toResource, // Usa la referencia al funcion del mapper
                        (existing, replacement) -> existing // En caso de duplicados, conserva el existente
                ));
    }
}