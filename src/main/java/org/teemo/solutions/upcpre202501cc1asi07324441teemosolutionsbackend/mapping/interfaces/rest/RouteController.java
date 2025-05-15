package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<RouteCalculationResource> calculateOptimalRoute(@RequestBody RouteRequestResource request) {
        var result = routeService.calculateOptimalRoute(
                request.startPort(),
                request.endPort()
        );
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/between-ports")
    public ResponseEntity<RouteDistanceResource> getDistanceBetweenPorts(
            @RequestParam String startPort,
            @RequestParam String endPort) {
        return routeService.getDistanceBetweenPorts(startPort, endPort)
                .map(distance -> new RouteDistanceResource(startPort, endPort, distance))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}