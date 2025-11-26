package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.PortOverviewService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.PortOperationalStatus;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.PortOverviewItemResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.PortOverviewResponse;

import java.util.List;

@RestController
@RequestMapping("/api/ports")
@Tag(name = "Ports", description = "Global port overview endpoints")
public class PortOverviewController {

    private final PortOverviewService portOverviewService;

    public PortOverviewController(PortOverviewService portOverviewService) {
        this.portOverviewService = portOverviewService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Listado paginado del estado global de puertos")
    public ResponseEntity<PortOverviewResponse> overview(
            @Parameter(description = "Filtra por estado operativo del puerto")
            @RequestParam(value = "state", required = false)
            @Schema(implementation = PortOperationalStatus.class)
            String state,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        PortOperationalStatus statusFilter = state != null && !state.isBlank()
                ? PortOperationalStatus.valueOf(state.toUpperCase())
                : null;
        var result = portOverviewService.overview(statusFilter, Math.max(page, 0), safeSize);
        List<PortOverviewItemResource> content = result.content().stream()
                .map(snapshot -> new PortOverviewItemResource(
                        snapshot.portId(),
                        snapshot.name(),
                        snapshot.country(),
                        snapshot.lat(),
                        snapshot.lon(),
                        snapshot.status(),
                        snapshot.reason(),
                        snapshot.traffic(),
                        snapshot.updatedAt(),
                        snapshot.contactPhone(),
                        snapshot.contactEmail(),
                        snapshot.website()
                ))
                .toList();
        return ResponseEntity.ok(new PortOverviewResponse(content, result.totalElements(), result.lastSyncedAt()));
    }
}
