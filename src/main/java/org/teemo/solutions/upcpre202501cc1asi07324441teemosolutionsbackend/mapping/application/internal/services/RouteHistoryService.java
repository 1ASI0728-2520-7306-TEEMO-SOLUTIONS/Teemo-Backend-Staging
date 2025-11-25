package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteHistory;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.RouteHistoryNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistorySource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.RouteHistoryStatus;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.mappers.RouteHistoryMapper;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteHistoryDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.queries.RouteHistorySearchCriteria;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RouteHistoryRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RouteHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(RouteHistoryService.class);

    private final RouteHistoryRepository repository;
    private final RouteHistoryMapper mapper;
    private final MeterRegistry meterRegistry;
    private final Clock clock;

    public RouteHistoryService(RouteHistoryRepository repository,
                               RouteHistoryMapper mapper,
                               MeterRegistry meterRegistry,
                               Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.meterRegistry = meterRegistry;
        this.clock = clock;
    }

    public RouteHistory save(RouteHistoryPersistRequest request) {
        Instant computedAt = clock.instant();
        RouteHistoryStatus status = Optional.ofNullable(request.status()).orElse(RouteHistoryStatus.SUCCESS);
        RouteHistorySource source = Optional.ofNullable(request.source()).orElse(RouteHistorySource.MANUAL);
        RouteHistory history = new RouteHistory(
                null,
                request.tenantId(),
                request.userId(),
                request.routeId(),
                request.originPortId(),
                request.originPortName(),
                request.destinationPortId(),
                request.destinationPortName(),
                request.waypointPortIds(),
                request.avoidedPortIds(),
                computedAt,
                request.engineVersion(),
                request.totalDistance(),
                request.durationEstimate(),
                request.costEstimate(),
                status,
                source,
                request.notes(),
                request.pathEncoding(),
                request.geojson(),
                buildDedupHash(request, computedAt),
                false,
                mergeMetadata(request.metadata())
        );

        RouteHistoryDocument saved = repository.save(mapper.toDocument(history));
        meterRegistry.counter("route_history_saved_total", "status", status.name()).increment();
        logger.info("route.history.saved routeId={} userId={} status={} avoidedCount={}",
                request.routeId(), request.userId(), status, size(request.avoidedPortIds()));
        return mapper.toDomain(saved);
    }

    public Page<RouteHistory> findHistory(RouteHistoryQuery query, Pageable pageable) {
        meterRegistry.counter("route_history_list_requests_total").increment();
        RouteHistorySearchCriteria criteria = new RouteHistorySearchCriteria(
                query.tenantId(),
                query.userId(),
                query.from(),
                query.to(),
                query.status(),
                query.source(),
                query.archived(),
                query.routeId()
        );
        Page<RouteHistoryDocument> page = repository.search(criteria, pageable);
        List<RouteHistory> domainPage = page.getContent().stream()
                .map(mapper::toDomain)
                .toList();
        return new PageImpl<>(domainPage, pageable, page.getTotalElements());
    }

    public Optional<RouteHistory> findById(String historyId) {
        return repository.findById(historyId).map(mapper::toDomain);
    }

    public RouteHistory archive(String historyId, String notes, String actorId) {
        RouteHistoryDocument document = repository.findById(historyId)
                .orElseThrow(() -> new RouteHistoryNotFoundException(historyId));
        if (document.isArchived()) {
            return mapper.toDomain(document);
        }
        document.setArchived(true);
        if (StringUtils.hasText(notes)) {
            document.setNotes(notes);
        }
        RouteHistoryDocument saved = repository.save(document);
        logger.info("route.history.archived historyId={} actorId={}", historyId, actorId);
        return mapper.toDomain(saved);
    }

    private Map<String, Object> mergeMetadata(Map<String, Object> metadata) {
        Map<String, Object> result = new HashMap<>();
        if (metadata != null) {
            result.putAll(metadata);
        }
        result.putIfAbsent("schemaVersion", 1);
        return result;
    }

    private String buildDedupHash(RouteHistoryPersistRequest request, Instant computedAt) {
        Instant rounded = computedAt.truncatedTo(ChronoUnit.MINUTES);
        String payload = String.join("|",
                nullToEmpty(request.originPortId()),
                nullToEmpty(request.destinationPortId()),
                String.join(",", request.waypointPortIds() == null ? List.of() : request.waypointPortIds()),
                String.join(",", request.avoidedPortIds() == null ? List.of() : request.avoidedPortIds()),
                rounded.toString()
        );
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to build dedup hash", e);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private int size(List<String> list) {
        return list == null ? 0 : list.size();
    }
}
