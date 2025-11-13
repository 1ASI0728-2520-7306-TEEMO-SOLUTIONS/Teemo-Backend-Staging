package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteHistoryQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteHistoryService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteHistory;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistorySource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.mappers.RouteHistoryMapper;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.ArchiveRouteHistoryRequest;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteHistoryItemResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteHistoryListResponse;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security.RoutingActorContext;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security.RoutingActorContextProvider;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Route History", description = "Route history endpoints")
public class RouteHistoryController {

    private final RouteHistoryService routeHistoryService;
    private final RouteHistoryMapper routeHistoryMapper;
    private final RoutingActorContextProvider actorContextProvider;

    public RouteHistoryController(RouteHistoryService routeHistoryService,
                                  RouteHistoryMapper routeHistoryMapper,
                                  RoutingActorContextProvider actorContextProvider) {
        this.routeHistoryService = routeHistoryService;
        this.routeHistoryMapper = routeHistoryMapper;
        this.actorContextProvider = actorContextProvider;
    }

    @GetMapping("/users/{userId}/route-history")
    @PreAuthorize("@routeHistoryAccessManager.canReadHistory(#userId)")
    @Operation(summary = "Listado paginado del historial de rutas del usuario")
    public ResponseEntity<RouteHistoryListResponse> getRouteHistory(
            @PathVariable String userId,
            @Parameter(description = "Fecha mínima (ISO-8601)") @RequestParam(value = "from", required = false) String from,
            @Parameter(description = "Fecha máxima (ISO-8601)") @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "archived", required = false) Boolean archived,
            @RequestParam(value = "routeId", required = false) String routeId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        RoutingActorContext actor = actorContextProvider.currentActor();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "computedAt"));
        RouteHistoryQuery query = new RouteHistoryQuery(
                actor.tenantId(),
                userId,
                parseInstant(from, "from"),
                parseInstant(to, "to"),
                parseStatus(status),
                parseSource(source),
                archived,
                routeId
        );

        Page<RouteHistory> historyPage = routeHistoryService.findHistory(query, pageable);
        List<RouteHistoryItemResource> resources = historyPage.getContent().stream()
                .map(routeHistoryMapper::toResource)
                .toList();
        String nextCursor = historyPage.hasNext() && !historyPage.getContent().isEmpty()
                ? historyPage.getContent().get(historyPage.getContent().size() - 1).id()
                : null;

        RouteHistoryListResponse response = new RouteHistoryListResponse(
                resources,
                page,
                size,
                historyPage.getTotalElements(),
                historyPage.getTotalPages(),
                nextCursor
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/route-history/{historyId}")
    @PreAuthorize("@routeHistoryAccessManager.canReadHistoryItem(#historyId)")
    @Operation(summary = "Obtiene un item específico del historial de rutas")
    public ResponseEntity<RouteHistoryItemResource> getHistoryItem(@PathVariable String historyId) {
        return routeHistoryService.findById(historyId)
                .map(routeHistoryMapper::toResource)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route history item not found"));
    }

    @PatchMapping("/route-history/{historyId}/archive")
    @PreAuthorize("@routeHistoryAccessManager.canArchiveHistoryItem(#historyId)")
    @Operation(summary = "Marca un item del historial como archivado")
    public ResponseEntity<RouteHistoryItemResource> archiveHistoryItem(@PathVariable String historyId,
                                                                       @RequestBody(required = false) ArchiveRouteHistoryRequest request,
                                                                       Authentication authentication) {
        String actorId = authentication != null ? authentication.getName() : "system";
        String notes = request != null ? request.notes() : null;
        RouteHistory archived = routeHistoryService.archive(historyId, notes, actorId);
        return ResponseEntity.ok(routeHistoryMapper.toResource(archived));
    }

    private Instant parseInstant(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format for %s".formatted(field));
        }
    }

    private RouteHistoryStatus parseStatus(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return RouteHistoryStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value: " + value);
        }
    }

    private RouteHistorySource parseSource(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return RouteHistorySource.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid source value: " + value);
        }
    }

}
