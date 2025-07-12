// PortRepository.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.PortDocument;

import java.util.Optional;

@Repository
public interface PortRepository extends MongoRepository<PortDocument, String> {

    Optional<PortDocument> findByNameAndContinent(String name, String continent);
    void deleteById(String id);
    Optional<PortDocument> findByName(String name);
    boolean existsByNameAndContinent(String name, String continent);
    void deleteAll();
}