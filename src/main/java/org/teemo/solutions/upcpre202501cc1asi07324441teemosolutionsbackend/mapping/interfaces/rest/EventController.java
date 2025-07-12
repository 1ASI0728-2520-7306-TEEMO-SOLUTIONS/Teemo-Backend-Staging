package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Endpoints para gestionar eventos geopolíticos y de navegación")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * Endpoint para obtener una lista de todos los puertos de origen únicos
     * que han sido registrados en algún evento.
     *
     * @return ResponseEntity con una lista de nombres de puertos.
     */
    @GetMapping("/origin-ports")
    public ResponseEntity<List<String>> getDistinctOriginPorts() {
        List<String> originPorts = eventService.getAllDistinctOriginPorts();
        return ResponseEntity.ok(originPorts);
    }
}