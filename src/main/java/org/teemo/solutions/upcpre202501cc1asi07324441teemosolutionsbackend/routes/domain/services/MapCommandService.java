package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Map;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreateMapCommand;

import java.util.Optional;

public interface MapCommandService {
    Optional<Map> handle(CreateMapCommand command);
}
