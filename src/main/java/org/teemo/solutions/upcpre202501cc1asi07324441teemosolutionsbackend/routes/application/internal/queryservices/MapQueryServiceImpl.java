package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Map;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetMapByIdQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services.MapQueryService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.infrastructure.persistence.jpa.repositories.MapRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MapQueryServiceImpl implements MapQueryService {

    private final MapRepository mapRepository;

    @Override
    public Optional<Map> handle(GetMapByIdQuery query) {
        return mapRepository.findById(query.idMap());
    }
}
