package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteHistoryDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.queries.RouteHistorySearchCriteria;

public interface RouteHistoryRepositoryCustom {
    Page<RouteHistoryDocument> search(RouteHistorySearchCriteria criteria, Pageable pageable);
}
