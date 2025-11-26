package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RoutePopularityService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RoutePopularityDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.PopularRouteResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security.RoutingActorContextProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteControllerPopularRoutesTest {

    @Mock
    private RouteService routeService;
    @Mock
    private RoutePopularityService routePopularityService;
    @Mock
    private RoutingActorContextProvider routingActorContextProvider;
    @InjectMocks
    private RouteController routeController;

    private RoutePopularityDocument sampleDocument;

    @BeforeEach
    void setUp() {
        sampleDocument = RoutePopularityDocument.builder()
                .routeId("route-1")
                .originPortId("origin-1")
                .originPortName("Origin")
                .destinationPortId("destination-1")
                .destinationPortName("Destination")
                .searchesCount(7L)
                .build();
    }

    @Test
    void getPopularRoutesReturnsMappedResources() {
        when(routePopularityService.findTopRoutes(8)).thenReturn(List.of(sampleDocument));

        ResponseEntity<List<PopularRouteResource>> response = routeController.getPopularRoutes(8);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        PopularRouteResource resource = response.getBody().get(0);
        assertThat(resource.originPortId()).isEqualTo("origin-1");
        assertThat(resource.destinationPortName()).isEqualTo("Destination");
        assertThat(resource.searchesCount()).isEqualTo(7L);
    }

    @Test
    void getPopularRoutesUsesDefaultLimitWhenNegative() {
        when(routePopularityService.findTopRoutes(8)).thenReturn(List.of());

        routeController.getPopularRoutes(-5);

        verify(routePopularityService).findTopRoutes(8);
    }
}
