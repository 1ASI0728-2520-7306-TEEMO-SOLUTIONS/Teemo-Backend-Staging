package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;

import java.util.Optional;

@Repository
public class MongoPortRepository {

    private final MongoTemplate mongoTemplate;

    public MongoPortRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Método de ejemplo personalizado
    public Optional<Port> findByNameCustom(String name) {
        Query query = new Query(Criteria.where("name").is(name));
        return Optional.ofNullable(mongoTemplate.findOne(query, Port.class));
    }
}