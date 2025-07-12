package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.RouteNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/routes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Route", description = "Route Endpoints")
public class RouteController {

    Logger logger = org.slf4j.LoggerFactory.getLogger(RouteController.class);
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    // Endpoint para calcular la ruta óptima entre dos puertos
    @PostMapping("/calculate-optimal-route")
    public ResponseEntity<RouteCalculationResource> calculateOptimalRoute(
            @Parameter(description = "Puerto de origen", required = true)
            @RequestParam String startPort,

            @Parameter(description = "Puerto de destino", required = true)
            @RequestParam String endPort) {
        try {
            RouteCalculationResource optimalRoute = routeService.calculateOptimalRoute(startPort, endPort);
            return ResponseEntity.ok(optimalRoute);
        } catch (PortNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RouteCalculationResource(
                            List.of(),
                            0.0,
                            List.of("Error: " + e.getMessage()),
                            Map.of()
                    ));
        } catch (RouteNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RouteCalculationResource(
                            List.of(),
                            0.0,
                            List.of("Ruta no disponible: " + e.getMessage()),
                            Map.of()
                    ));
        } catch (Exception e) {
            // Log detallado para el desarrollador
            logger.error("Error interno: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RouteCalculationResource(
                            List.of(),
                            0.0,
                            List.of("Error interno. Revisa el Log interno."),
                            Map.of()
                    ));
        }
    }

    // Endpoint para obtener la distancia entre dos puertos específicos
    @Operation(summary = "Obtiene distancia entre dos puertos")
    @GetMapping("/distance-between-ports")
    public ResponseEntity<RouteDistanceResource> getDistanceBetweenPorts(
            @Parameter(description = "Puerto de origen", required = true)
            @RequestParam String startPort,

            @Parameter(description = "Puerto de destino", required = true)
            @RequestParam String endPort) {
        try {
            // 1. Obtener la ruta óptima usando el servicio existente
            RouteCalculationResource optimalRoute = routeService.calculateOptimalRoute(startPort, endPort);

            // 2. Extraer la distancia total y mensajes relevantes
            double distance = optimalRoute.totalDistance();
            List<String> messages = optimalRoute.warnings();

            // 3. Construir metadata con los puertos
            Map<String, Object> meta = Map.of(
                    "startPort", startPort,
                    "endPort", endPort,
                    "routeSteps", optimalRoute.optimalRoute().size()
            );

            // 4. Retornar respuesta exitosa
            return ResponseEntity.ok(
                    new RouteDistanceResource(distance, messages, meta)
            );

        } catch (IllegalStateException e) {
            // Manejar errores de negocio (ej: ruta no encontrada)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RouteDistanceResource(
                            0.0,
                            List.of(e.getMessage()),
                            Map.of()
                    ));
        } catch (Exception e) {
            // Manejar errores inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RouteDistanceResource(
                            0.0,
                            List.of("Error interno: " + e.getMessage()),
                            Map.of()
                    ));
        }
    }

    // Endpoint para obtener todas las rutas
    @GetMapping("/all-routes")
    public ResponseEntity<List<RouteDocument>> getAllRoutes() {
        List<RouteDocument> routesPage = routeService.findAllRoutes();
        return ResponseEntity.ok(routesPage);
    }
}