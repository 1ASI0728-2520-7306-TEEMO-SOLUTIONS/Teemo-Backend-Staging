package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetAllPortsQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetPortByIdQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetPortByNameQuery;

import java.util.List;
import java.util.Optional;

public interface PortQueryService {
    List<Port> handle(GetAllPortsQuery query);
    Optional<Port> handle(GetPortByIdQuery query);
    Optional<Port> handle(GetPortByNameQuery query);
}
