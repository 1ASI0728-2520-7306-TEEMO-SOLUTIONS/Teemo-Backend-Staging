package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreatePortCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services.PortCommandService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.infrastructure.persistence.jpa.repositories.PortRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortCommandServiceImpl implements PortCommandService {

    private final PortRepository portRepository;

    @Override
    public Optional<Port> handle(CreatePortCommand command) {
        Port newPort = new Port(command);
        return Optional.of(portRepository.save(newPort));
    }
}
