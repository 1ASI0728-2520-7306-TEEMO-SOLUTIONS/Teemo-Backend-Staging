package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreatePortCommand;

import java.util.Optional;

public interface PortCommandService {
    Optional<Port> handle(CreatePortCommand command);
}
