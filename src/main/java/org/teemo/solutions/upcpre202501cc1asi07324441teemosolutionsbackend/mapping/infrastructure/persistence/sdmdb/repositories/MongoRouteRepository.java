package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;

import java.util.Optional;

@Repository
public class MongoRouteRepository {

    private final MongoTemplate mongoTemplate;
    private final RouteRepository routeRepository;

    public MongoRouteRepository(MongoTemplate mongoTemplate, RouteRepository routeRepository) {
        this.mongoTemplate = mongoTemplate;
        this.routeRepository = routeRepository;
    }

    public Optional<Double> findDistanceBetweenPorts(String start, String end) {
        return routeRepository.findBetweenPorts(start, end)
                .map(RouteDocument::getDistance);
    }

    // Método de ejemplo personalizado
    public void customSave(RouteDocument route) {
        mongoTemplate.save(route);
    }
}