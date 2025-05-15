package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.EventDocument;

import java.util.List;

public interface EventRepository extends MongoRepository<EventDocument, String> {
    List<EventDocument> findByPuertoOrigen(String puertoOrigen);
}
