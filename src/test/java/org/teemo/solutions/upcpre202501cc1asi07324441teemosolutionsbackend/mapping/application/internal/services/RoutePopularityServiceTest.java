package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RoutePopularityDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.RoutePopularityRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutePopularityServiceTest {

    @Mock
    private RoutePopularityRepository routePopularityRepository;

    @InjectMocks
    private RoutePopularityService routePopularityService;

    private Port origin;
    private Port destination;

    @BeforeEach
    void setUp() {
        origin = new Port("origin-1", "Origin", new Coordinates(10.0, 20.0), "EU");
        destination = new Port("destination-1", "Destination", new Coordinates(-5.0, 5.0), "US");
    }

    @Test
    void registerSearchCreatesNewDocumentWhenCombinationDoesNotExist() {
        when(routePopularityRepository.findByOriginPortIdAndDestinationPortId("origin-1", "destination-1"))
                .thenReturn(Optional.empty());

        routePopularityService.registerSearch(origin, destination, "route-123");

        ArgumentCaptor<RoutePopularityDocument> captor = ArgumentCaptor.forClass(RoutePopularityDocument.class);
        verify(routePopularityRepository).save(captor.capture());

        RoutePopularityDocument saved = captor.getValue();
        assertThat(saved.getOriginPortId()).isEqualTo("origin-1");
        assertThat(saved.getDestinationPortId()).isEqualTo("destination-1");
        assertThat(saved.getSearchesCount()).isEqualTo(1L);
        assertThat(saved.getRouteId()).isEqualTo("route-123");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getLastSearchedAt()).isNotNull();
    }

    @Test
    void registerSearchIncrementsExistingDocument() {
        Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
        RoutePopularityDocument existing = RoutePopularityDocument.builder()
                .id("doc-1")
                .originPortId("origin-1")
                .destinationPortId("destination-1")
                .originPortName("Old Origin")
                .destinationPortName("Old Destination")
                .createdAt(createdAt)
                .lastSearchedAt(createdAt)
                .searchesCount(4L)
                .build();

        when(routePopularityRepository.findByOriginPortIdAndDestinationPortId("origin-1", "destination-1"))
                .thenReturn(Optional.of(existing));

        routePopularityService.registerSearch(origin, destination, null);

        verify(routePopularityRepository).save(existing);
        assertThat(existing.getSearchesCount()).isEqualTo(5L);
        assertThat(existing.getCreatedAt()).isEqualTo(createdAt);
        assertThat(existing.getOriginPortName()).isEqualTo("Origin");
        assertThat(existing.getDestinationPortName()).isEqualTo("Destination");
        assertThat(existing.getLastSearchedAt()).isNotNull();
    }

    @Test
    void findTopRoutesCapsLimitAndDelegatesToRepository() {
        RoutePopularityDocument document = RoutePopularityDocument.builder()
                .originPortId("origin-1")
                .destinationPortId("destination-1")
                .searchesCount(9L)
                .build();
        when(routePopularityRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(document)));

        List<RoutePopularityDocument> result = routePopularityService.findTopRoutes(100);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(routePopularityRepository).findAll(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertThat(result).containsExactly(document);
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(50);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "searchesCount"));
    }

    @Test
    void findTopRoutesReturnsEmptyListWhenLimitInvalid() {
        List<RoutePopularityDocument> result = routePopularityService.findTopRoutes(0);

        assertThat(result).isEmpty();
        verify(routePopularityRepository, never()).findAll(any(PageRequest.class));
    }
}
