package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates.SafetyValidator;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.GeoUtils;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.RouteCalculatorService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.MongoPortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.MongoRouteRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.CoordinatesResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteCalculationResource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final Logger logger = LoggerFactory.getLogger(RouteService.class);
    private final MongoRouteRepository routeRepository;
    private final MongoPortRepository portRepository;
    private final RouteCalculatorService routeCalculatorService;
    private final SafetyValidator safetyValidator;

    @Autowired
    public RouteService (
            MongoRouteRepository routeRepository,
            MongoPortRepository portRepository,
            RouteCalculatorService routeCalculatorService,
            SafetyValidator safetyValidator
    ) {
        this.routeRepository = routeRepository;
        this.portRepository = portRepository;
        this.routeCalculatorService = routeCalculatorService;
        this.safetyValidator = safetyValidator;
    }


    public RouteCalculationResource calculateOptimalRoute(String startPortName, String endPortName) {

        // Buscar cualquier puerto origen (incluso si hay múltiples con el mismo nombre)
        Port startPort = portRepository.findByName(startPortName)
                .stream()
                .findFirst()
                .orElseThrow(() -> new PortNotFoundException("Puerto origen no encontrado: " + startPortName));

        // Buscar cualquier puerto destino (sin restricción de continente)
        Port endPort = portRepository.findByName(endPortName)
                .stream()
                .findFirst()
                .orElseThrow(() -> new PortNotFoundException("Puerto destino no encontrado: " + endPortName));





        // Calcular ruta óptima sin restricciones geográficas
        List<Port> optimalRoute = routeCalculatorService.calculateOptimalRoute(startPort, endPort);

        // Calcular distancia usando el repositorio
        double totalDistance = calculateTotalDistance(optimalRoute);

        // Validar seguridad
        List<String> warnings = safetyValidator.validateRoute(optimalRoute);

        return new RouteCalculationResource(
                optimalRoute.stream().map(Port::getName).toList(),
                totalDistance,
                warnings,
                createCoordinatesMapping(optimalRoute)
        );
    }

    public double calculateTotalDistance(List<Port> route) {
        double total = 0.0;
        GeoUtils geoUtils = new GeoUtils(); // Inyectar o instanciar

        for (int i = 0; i < route.size() - 1; i++) {
            Port currentPort = route.get(i);
            Port nextPort = route.get(i + 1);

            try {
                // 1. Intenta obtener ruta documentada
                RouteDocument routeDoc = routeRepository.getBetweenPorts(
                        currentPort.getName(),
                        nextPort.getName()
                );

                if (routeDoc != null) {
                    total += routeDoc.getDistance();
                } else {
                    // 2. Calcula distancia Haversine si no existe ruta registrada
                    double distance = geoUtils.calculateHaversineDistance(currentPort, nextPort);
                    logger.warn("Usando distancia estimada entre {} y {}: {} mn",
                            currentPort.getName(), nextPort.getName(), distance);
                    total += distance;
                }
            } catch (Exception e) {
                // 3. Fallback a cálculo geográfico
                double distance = geoUtils.calculateHaversineDistance(currentPort, nextPort);
                total += distance;
                logger.error("Error calculando ruta entre {} y {}: {}",
                        currentPort.getName(), nextPort.getName(), e.getMessage());
            }
        }
        return total;
    }

    // En RouteService.java
    public List<RouteDocument> getAllRoutes() {
        return routeRepository.getAll();
    }

    private Map<String, CoordinatesResource> createCoordinatesMapping(List<Port> ports) {
        return ports.stream()
                .collect(Collectors.toMap(
                        Port::getName,
                        port -> new CoordinatesResource(
                                port.getCoordinates().latitude(),
                                port.getCoordinates().longitude()
                        )));
    }
}