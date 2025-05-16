package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.exceptionhandlers.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates.SafetyValidator;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
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
        for (int i = 0; i < route.size() - 1; i++) {
            String current = route.get(i).getName();
            String next = route.get(i + 1).getName();

            RouteDocument calculateDistancePort = routeRepository.getBetweenPorts(current, next);

            Double distance = calculateDistancePort.getDistance();

            total += distance;
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