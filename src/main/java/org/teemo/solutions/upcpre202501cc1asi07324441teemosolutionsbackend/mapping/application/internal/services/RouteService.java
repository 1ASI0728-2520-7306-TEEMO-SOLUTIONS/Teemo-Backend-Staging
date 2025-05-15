package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.exceptionhandlers.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates.SafetyValidator;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.RouteCalculatorService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.CoordinatesResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteCalculationResource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final PortRepository portRepository;
    private final RouteCalculatorService routeCalculatorService;
    private final SafetyValidator safetyValidator;

    public RouteCalculationResource calculateOptimalRoute(String startPortName, String endPortName) {
        Port start = portRepository.findByName(startPortName)
                .orElseThrow(() -> new PortNotFoundException(startPortName));
        Port end = portRepository.findByName(endPortName)
                .orElseThrow(() -> new PortNotFoundException(endPortName));

        // Usar RouteCalculatorService para obtener la ruta óptima
        List<Port> optimalRoute = routeCalculatorService.calculateOptimalRoute(start, end);

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

    // Método para calcular la distancia total usando el repositorio
    private double calculateTotalDistance(List<Port> route) {
        double total = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            String current = route.get(i).getName();
            String next = route.get(i + 1).getName();

            Double distance = routeRepository.findBetweenPorts(current, next)
                    .map(RouteDocument::getDistance)
                    .orElseThrow(() -> new RuntimeException(
                            "Ruta no encontrada entre " + current + " y " + next
                    ));
            total += distance;
        }
        return total;
    }

    // En RouteService.java
    public Page<RouteDocument> getAllRoutes(Pageable pageable) {
        return routeRepository.findAll(pageable);
    }

    public Optional<Double> getDistanceBetweenPorts(String start, String end) {
        return routeRepository.findBetweenPorts(start, end)
                .map(RouteDocument::getDistance);
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