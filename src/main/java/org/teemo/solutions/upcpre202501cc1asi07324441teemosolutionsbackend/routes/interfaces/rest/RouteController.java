package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services.MapGraphService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources.RoutePlanRequest;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources.RoutePlanResult;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/routes")
@Tag(name = "Routes", description = "Route Management Endpoints")
@RequiredArgsConstructor
public class RouteController {

    private final MapGraphService mapGraphService;

    @GetMapping("/find")
    public ResponseEntity<List<PortResource>> findRoute(
            @RequestParam String from,
            @RequestParam String to) {

        var route = mapGraphService.findOptimalRoute(from, to);

        if (route.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var ports = route.stream()
                .map(name -> {
                    var port = mapGraphService.getPortByName(name);
                    if (port == null) {
                        throw new IllegalArgumentException("Puerto no encontrado: " + name);
                    }
                    return new PortResource(port.getName(), port.getLatitude(), port.getLongitude());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ports);
    }

    @PostMapping("/plan")
    public ResponseEntity<RoutePlanResult> planRoute(@RequestBody RoutePlanRequest request) {
        var route = mapGraphService.findOptimalRoute(request.from(), request.to());
        if (route.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RoutePlanResult(route, "No se encontró ruta"));
        }
        return ResponseEntity.ok(new RoutePlanResult(route, "Ruta encontrada"));
    }

    @GetMapping("/ports")
    public ResponseEntity<List<PortResource>> getAllPorts() {
        var ports = mapGraphService.getAllPorts();
        var resources = ports.stream()
                .map(port -> new PortResource(port.getName(), port.getLatitude(), port.getLongitude()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(resources);
    }

    public record PortResource(String name, Double lat, Double lng) {}
}
