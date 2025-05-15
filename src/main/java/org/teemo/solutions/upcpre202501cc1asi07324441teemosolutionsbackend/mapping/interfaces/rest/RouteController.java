package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // ✅ Importación correcta
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.*;

@RestController
@RequestMapping(value = "/api/routes", produces = MediaType.APPLICATION_JSON_VALUE) // 👈 Plural
@Tag(name = "Route", description = "Route Endpoints")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    // Endpoint para calcular la ruta óptima entre dos puertos
    @PostMapping("/calculate-optimal-route")
    public ResponseEntity<RouteCalculationResource> calculateOptimalRoute(@RequestBody RouteRequestResource request) {
        var result = routeService.calculateOptimalRoute(
                request.startPort(),
                request.endPort()
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // Endpoint para obtener la distancia entre dos puertos específicos
    @GetMapping("/distance-between-ports")
    public ResponseEntity<RouteDistanceResource> getDistanceBetweenPorts(
            @RequestParam String startPort,
            @RequestParam String endPort) {

        return routeService.getDistanceBetweenPorts(startPort, endPort)
                .map(distance -> new RouteDistanceResource(startPort, endPort, distance))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para obtener todas las rutas disponibles de forma paginada
    @GetMapping("/all-routes")
    public ResponseEntity<Page<RouteDistanceResource>> getAllRoutes(Pageable pageable) {
        return ResponseEntity.ok(
                routeService.getAllRoutes(pageable)
                        .map(route -> new RouteDistanceResource(
                                route.getHomePort(),
                                route.getDestinationPort(),
                                route.getDistance()
                        ))
        );
    }
}