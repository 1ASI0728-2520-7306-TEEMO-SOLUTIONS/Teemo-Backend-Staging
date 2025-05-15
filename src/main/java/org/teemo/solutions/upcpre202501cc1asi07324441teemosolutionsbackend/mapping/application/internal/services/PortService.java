package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.commands.CreatePortCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortService {

    private final PortRepository portRepository;

    public Port handle(CreatePortCommand command) {
        Port port = new Port(
                command.name(),
                command.coordinates(),
                command.continent()
        );
        return portRepository.save(port);
    }

    public Optional<Port> getPortById(String id) {
        return portRepository.findById(id);
    }

    public Optional<Port> getPortByName(String name) {
        return portRepository.findByName(name);
    }

    public Page<Port> getAllPorts(Pageable pageable) {
        return portRepository.findAll(pageable);
    }

    public void deletePort(String id) {
        portRepository.deleteById(id);
    }
}