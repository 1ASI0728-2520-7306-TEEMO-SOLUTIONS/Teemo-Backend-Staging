package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RoutePopularityDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RoutePopularityRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutePopularityService {

    private static final Logger logger = LoggerFactory.getLogger(RoutePopularityService.class);
    private static final int MAX_LIMIT = 50;

    private final RoutePopularityRepository routePopularityRepository;

    public void registerSearch(Port origin, Port destination, String routeId) {
        if (origin == null || destination == null) {
            logger.warn("route.popularity.skip missing origin/destination");
            return;
        }
        String originIdentifier = resolvePortIdentifier(origin);
        String destinationIdentifier = resolvePortIdentifier(destination);
        if (originIdentifier == null || destinationIdentifier == null) {
            logger.warn("route.popularity.skip missing identifiers origin={} destination={}",
                    origin.getName(), destination.getName());
            return;
        }

        Instant now = Instant.now();
        RoutePopularityDocument document = routePopularityRepository
                .findByOriginPortIdAndDestinationPortId(originIdentifier, destinationIdentifier)
                .orElseGet(() -> RoutePopularityDocument.builder()
                        .originPortId(originIdentifier)
                        .originPortName(origin.getName())
                        .destinationPortId(destinationIdentifier)
                        .destinationPortName(destination.getName())
                        .routeId(routeId)
                        .createdAt(now)
                        .lastSearchedAt(now)
                        .searchesCount(0L)
                        .build());

        document.setOriginPortName(origin.getName());
        document.setDestinationPortName(destination.getName());
        document.setRouteId(routeId);
        document.setLastSearchedAt(now);
        if (document.getCreatedAt() == null) {
            document.setCreatedAt(now);
        }
        document.setSearchesCount(document.getSearchesCount() + 1);

        routePopularityRepository.save(document);
    }

    public List<RoutePopularityDocument> findTopRoutes(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        int sanitizedLimit = Math.min(limit, MAX_LIMIT);
        PageRequest pageRequest = PageRequest.of(0, sanitizedLimit, Sort.by(Sort.Direction.DESC, "searchesCount"));
        return routePopularityRepository.findAll(pageRequest).getContent();
    }

    private String resolvePortIdentifier(Port port) {
        if (port == null) {
            return null;
        }
        return port.getId() != null ? port.getId() : port.getName();
    }
}
