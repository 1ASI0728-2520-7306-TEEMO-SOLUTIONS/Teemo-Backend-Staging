package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.EventDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.EventRepository;

import java.util.List;

// MongoEventRepository.java - Implementación Correcta
@Repository
public class MongoEventRepository implements EventRepository {

    private final MongoTemplate mongoTemplate;
    private static final String COLLECTION_NAME = "events-documents";

    public MongoEventRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<EventDocument> findValidEvents() {
        Query query = new Query(Criteria.where("puertoOrigen").exists(true)
                .andOperator(
                        Criteria.where("puertoOrigen").ne(null),
                        Criteria.where("puertoOrigen").ne("")
                ));
        return mongoTemplate.find(query, EventDocument.class, COLLECTION_NAME);
    }

    public List<String> findDistinctOriginPorts() {
        // Usa el nuevo nombre de campo "originPort"
        Query query = new Query(Criteria.where("puertoOrigen").exists(true).ne("").ne(null));

        // Usa el nuevo nombre de campo "originPort"
        return mongoTemplate.findDistinct(query, "puertoOrigen", COLLECTION_NAME, String.class);
    }
}
