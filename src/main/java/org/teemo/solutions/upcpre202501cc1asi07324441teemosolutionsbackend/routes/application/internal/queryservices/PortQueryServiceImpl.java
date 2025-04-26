package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetAllPortsQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetPortByIdQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.queries.GetPortByNameQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services.PortQueryService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.infrastructure.persistence.jpa.repositories.PortRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortQueryServiceImpl implements PortQueryService {

    private final PortRepository portRepository;

    @Override
    public List<Port> handle(GetAllPortsQuery query) {
        return portRepository.findAll();
    }

    @Override
    public Optional<Port> handle(GetPortByIdQuery query) {
        return portRepository.findById(query.portId());
    }

    @Override
    public Optional<Port> handle(GetPortByNameQuery query) {
        return portRepository.findByName(query.name());
    }
}
