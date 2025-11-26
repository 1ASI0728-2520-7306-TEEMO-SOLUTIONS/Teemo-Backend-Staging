package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RoutePopularityDocument;

import java.util.Optional;

@Repository
public interface RoutePopularityRepository extends MongoRepository<RoutePopularityDocument, String> {

    Optional<RoutePopularityDocument> findByOriginPortIdAndDestinationPortId(String originPortId, String destinationPortId);

    Page<RoutePopularityDocument> findAll(Pageable pageable);
}
