package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteHistoryQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteHistoryService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteReportService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteHistory;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.RouteHistoryNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteReportSummaryListResponse;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteReportSummaryResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security.RoutingActorContext;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security.RoutingActorContextProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class RouteReportController {

    private final RouteReportService routeReportService;
    private final RouteHistoryService routeHistoryService;
    private final RoutingActorContextProvider actorContextProvider;

    public RouteReportController(RouteReportService routeReportService,
                                 RouteHistoryService routeHistoryService,
                                 RoutingActorContextProvider actorContextProvider) {
        this.routeReportService = routeReportService;
        this.routeHistoryService = routeHistoryService;
        this.actorContextProvider = actorContextProvider;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RouteReportSummaryListResponse> listReports(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "archived", defaultValue = "false") boolean archived
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        RoutingActorContext actor = actorContextProvider.currentActor();
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "computedAt"));
        RouteHistoryQuery query = new RouteHistoryQuery(
                actor.tenantId(),
                actor.userId(),
                null,
                null,
                null,
                null,
                archived ? null : Boolean.FALSE,
                null
        );
        Page<RouteHistory> result = routeHistoryService.findHistory(query, pageable);
        List<RouteReportSummaryResource> items = result.getContent().stream()
                .map(this::toSummaryResource)
                .toList();
        RouteReportSummaryListResponse response = new RouteReportSummaryListResponse(
                items,
                page,
                safeSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping(value = "/{historyId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("@routeHistoryAccessManager.canReadHistoryItem(#historyId)")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String historyId) {
        try {
            byte[] payload = routeReportService.generatePdfReport(historyId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, buildAttachment("pdf", historyId))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(payload);
        } catch (RouteHistoryNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping(value = "/{historyId}/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("@routeHistoryAccessManager.canReadHistoryItem(#historyId)")
    public ResponseEntity<byte[]> downloadExcel(@PathVariable String historyId) {
        try {
            byte[] payload = routeReportService.generateExcelReport(historyId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, buildAttachment("xlsx", historyId))
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(payload);
        } catch (RouteHistoryNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    private RouteReportSummaryResource toSummaryResource(RouteHistory history) {
        Instant departure = history.computedAt();
        Instant eta = computeEta(departure, history.durationEstimate());
        double progress = computeProgress(history.status(), departure, eta);
        String routeLabel = formatRoute(
                history.originPortName(),
                history.originPortId(),
                history.destinationPortName(),
                history.destinationPortId()
        );
        return new RouteReportSummaryResource(
                history.id(),
                history.routeId() != null ? history.routeId() : history.id(),
                routeLabel,
                resolveName(history.originPortName(), history.originPortId()),
                resolveName(history.destinationPortName(), history.destinationPortId()),
                history.status(),
                progress,
                departure,
                eta,
                history.totalDistance(),
                history.costEstimate()
        );
    }

    private Instant computeEta(Instant departure, Double durationHours) {
        if (departure == null || durationHours == null) {
            return null;
        }
        long minutes = Math.round(durationHours * 60);
        return departure.plus(Duration.ofMinutes(minutes));
    }

    private double computeProgress(RouteHistoryStatus status, Instant departure, Instant eta) {
        if (status == RouteHistoryStatus.SUCCESS) {
            return 1.0;
        }
        if (status == RouteHistoryStatus.CANCELLED || status == RouteHistoryStatus.NO_VIABLE_ROUTE) {
            return 0.0;
        }
        if (departure == null || eta == null) {
            return 0.0;
        }
        Instant now = Instant.now();
        if (now.isBefore(departure)) {
            return 0.0;
        }
        long totalSeconds = Duration.between(departure, eta).toSeconds();
        if (totalSeconds <= 0) {
            return 1.0;
        }
        long elapsedSeconds = Duration.between(departure, now).toSeconds();
        double ratio = (double) elapsedSeconds / (double) totalSeconds;
        return Math.max(0.0, Math.min(1.0, ratio));
    }

    private String formatRoute(String origin, String originFallback, String destination, String destinationFallback) {
        return "%s -> %s".formatted(
                resolveName(origin, originFallback),
                resolveName(destination, destinationFallback)
        );
    }

    private String resolveName(String name, String fallback) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return fallback != null ? fallback : "-";
    }

    private String buildAttachment(String extension, String historyId) {
        return "attachment; filename=\"route-report-%s.%s\"".formatted(historyId, extension);
    }
}
