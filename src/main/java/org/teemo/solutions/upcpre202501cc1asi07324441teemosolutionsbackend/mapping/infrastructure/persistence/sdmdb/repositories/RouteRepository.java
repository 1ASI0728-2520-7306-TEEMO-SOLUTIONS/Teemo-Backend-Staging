// RouteRepository.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;
import java.util.Optional;

public interface RouteRepository extends MongoRepository<RouteDocument, String> {

    @Query("{ $or: [ "
            + "{ homePort: ?0, destinationPort: ?1 }, "
            + "{ homePort: ?1, destinationPort: ?0 } "
            + "] }")
    Optional<RouteDocument> findBetweenPorts(String port1, String port2);

    Page<RouteDocument> findAll(Pageable pageable);

}