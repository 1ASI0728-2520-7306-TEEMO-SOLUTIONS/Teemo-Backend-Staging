package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.mappers;

import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteHistory;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteHistoryDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.RouteHistoryItemResource;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class RouteHistoryMapper {

    public RouteHistory toDomain(RouteHistoryDocument document) {
        if (document == null) {
            return null;
        }
        return new RouteHistory(
                document.getId(),
                document.getTenantId(),
                document.getUserId(),
                document.getRouteId(),
                document.getOriginPortId(),
                document.getOriginPortName(),
                document.getDestinationPortId(),
                document.getDestinationPortName(),
                document.getWaypointPortIds(),
                document.getAvoidedPortIds(),
                document.getComputedAt(),
                document.getEngineVersion(),
                document.getTotalDistance(),
                document.getDurationEstimate(),
                document.getCostEstimate(),
                document.getStatus(),
                document.getSource(),
                document.getNotes(),
                document.getPathEncoding(),
                document.getGeojson(),
                document.getDedupHash(),
                document.isArchived(),
                document.getMetadata()
        );
    }

    public RouteHistoryDocument toDocument(RouteHistory routeHistory) {
        if (routeHistory == null) {
            return null;
        }
        return RouteHistoryDocument.builder()
                .id(routeHistory.id())
                .tenantId(routeHistory.tenantId())
                .userId(routeHistory.userId())
                .routeId(routeHistory.routeId())
                .originPortId(routeHistory.originPortId())
                .originPortName(routeHistory.originPortName())
                .destinationPortId(routeHistory.destinationPortId())
                .destinationPortName(routeHistory.destinationPortName())
                .waypointPortIds(routeHistory.waypointPortIds())
                .avoidedPortIds(routeHistory.avoidedPortIds())
                .computedAt(routeHistory.computedAt())
                .engineVersion(routeHistory.engineVersion())
                .totalDistance(routeHistory.totalDistance())
                .durationEstimate(routeHistory.durationEstimate())
                .costEstimate(routeHistory.costEstimate())
                .status(routeHistory.status())
                .source(routeHistory.source())
                .notes(routeHistory.notes())
                .pathEncoding(routeHistory.pathEncoding())
                .geojson(routeHistory.geojson())
                .dedupHash(routeHistory.dedupHash())
                .archived(routeHistory.archived())
                .metadata(routeHistory.metadata())
                .build();
    }

    public RouteHistoryItemResource toResource(RouteHistory routeHistory) {
        if (routeHistory == null) {
            return null;
        }
        return new RouteHistoryItemResource(
                routeHistory.id(),
                routeHistory.tenantId(),
                routeHistory.userId(),
                routeHistory.routeId(),
                routeHistory.originPortId(),
                routeHistory.originPortName(),
                routeHistory.destinationPortId(),
                routeHistory.destinationPortName(),
                routeHistory.waypointPortIds(),
                routeHistory.avoidedPortIds(),
                routeHistory.computedAt(),
                routeHistory.engineVersion(),
                routeHistory.totalDistance(),
                routeHistory.durationEstimate(),
                routeHistory.costEstimate(),
                routeHistory.status(),
                routeHistory.source(),
                routeHistory.notes(),
                routeHistory.archived(),
                routeHistory.metadata(),
                routeHistory.pathEncoding(),
                routeHistory.geojson()
        );
    }

    public List<RouteHistoryItemResource> toResourceList(List<RouteHistory> histories) {
        return histories == null ? List.of() : histories.stream()
                .map(this::toResource)
                .filter(Objects::nonNull)
                .toList();
    }
}
