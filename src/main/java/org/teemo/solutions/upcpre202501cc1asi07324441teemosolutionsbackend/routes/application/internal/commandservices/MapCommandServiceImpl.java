package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Map;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreateMapCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services.MapCommandService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.infrastructure.persistence.jpa.repositories.MapRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MapCommandServiceImpl implements MapCommandService {

    private final MapRepository mapRepository;

    @Override
    public Optional<Map> handle(CreateMapCommand command) {
        Map map = new Map(command);
        return Optional.of(mapRepository.save(map));
    }
}
