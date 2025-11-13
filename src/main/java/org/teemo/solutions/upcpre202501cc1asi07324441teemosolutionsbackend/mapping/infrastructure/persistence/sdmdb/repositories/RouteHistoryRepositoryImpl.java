package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.RouteHistoryDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.queries.RouteHistorySearchCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class RouteHistoryRepositoryImpl implements RouteHistoryRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public RouteHistoryRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<RouteHistoryDocument> search(RouteHistorySearchCriteria criteria, Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable is required");
        RouteHistorySearchCriteria safeCriteria = criteria != null ? criteria : new RouteHistorySearchCriteria(null, null, null, null, null, null, null, null);

        List<Criteria> definitions = new ArrayList<>();
        if (safeCriteria.userId() != null) {
            definitions.add(Criteria.where("userId").is(safeCriteria.userId()));
        }
        if (safeCriteria.tenantId() != null) {
            definitions.add(Criteria.where("tenantId").is(safeCriteria.tenantId()));
        }
        if (safeCriteria.routeId() != null) {
            definitions.add(Criteria.where("routeId").is(safeCriteria.routeId()));
        }
        if (safeCriteria.status() != null) {
            definitions.add(Criteria.where("status").is(safeCriteria.status()));
        }
        if (safeCriteria.source() != null) {
            definitions.add(Criteria.where("source").is(safeCriteria.source()));
        }
        if (safeCriteria.archived() != null) {
            definitions.add(Criteria.where("archived").is(safeCriteria.archived()));
        }
        if (safeCriteria.from() != null || safeCriteria.to() != null) {
            Criteria dateCriteria = Criteria.where("computedAt");
            if (safeCriteria.from() != null) {
                dateCriteria = dateCriteria.gte(safeCriteria.from());
            }
            if (safeCriteria.to() != null) {
                dateCriteria = dateCriteria.lte(safeCriteria.to());
            }
            definitions.add(dateCriteria);
        }

        Query query = new Query();
        definitions.forEach(query::addCriteria);
        Query countQuery = new Query();
        definitions.forEach(countQuery::addCriteria);

        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "computedAt");
        query.with(sort);
        query.with(pageable);

        long total = mongoTemplate.count(countQuery, RouteHistoryDocument.class);
        List<RouteHistoryDocument> content = mongoTemplate.find(query, RouteHistoryDocument.class);
        return new PageImpl<>(content, pageable, total);
    }
}
