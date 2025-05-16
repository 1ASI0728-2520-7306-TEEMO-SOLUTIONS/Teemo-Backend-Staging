package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;

import java.util.List;

@Repository
public class MongoRouteRepository implements RouteRepository {

    MongoTemplate mongoTemplate;

    public MongoRouteRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public RouteDocument getBetweenPorts(String port1, String port2) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("Home Port").is(port1).and("Destination Port").is(port2),
                Criteria.where("Home Port").is(port2).and("Destination Port").is(port1)
        );

        return mongoTemplate.findOne(
                Query.query(criteria),
                RouteDocument.class
        );
    }

    @Override
    public void saveAll(List<RouteDocument> routes) {
        for (RouteDocument route : routes) {
            mongoTemplate.save(route);
        }
    }

    @Override
    public List<RouteDocument> getAll() {
        return mongoTemplate.findAll(RouteDocument.class);
    }
}
