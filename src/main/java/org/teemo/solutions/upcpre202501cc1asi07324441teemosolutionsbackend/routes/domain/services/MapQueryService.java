package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Map;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetMapByIdQuery;

import java.util.Optional;

public interface MapQueryService {
    Optional<Map> handle(GetMapByIdQuery query);
}
