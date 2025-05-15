// PortRepository.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import java.util.Optional;

public interface PortRepository extends MongoRepository<Port, String> {
    Optional<Port> findByName(String name);
}