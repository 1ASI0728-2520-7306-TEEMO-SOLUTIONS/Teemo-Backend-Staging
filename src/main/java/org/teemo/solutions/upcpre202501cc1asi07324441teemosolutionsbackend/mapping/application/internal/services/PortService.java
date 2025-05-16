package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.commands.CreatePortCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.MongoPortRepository;

import java.util.List;
import java.util.Optional;

@Service

public class PortService {

    private final MongoPortRepository portRepository;

    @Autowired
    public PortService (MongoPortRepository portRepository) {
        this.portRepository = portRepository;
    }

    public Port createPort(CreatePortCommand command) {
        Port port = new Port(
                command.name(),
                command.coordinates(),
                command.continent()
        );
        return portRepository.savePort(port);
    }

    public Optional<Port> getPortById(String id) {
        return portRepository.findById(id);
    }

    public Optional<Port> getPortByName(String name) {
        List<Port> ports = portRepository.findByName(name);

        if (ports.size() > 1) {
            throw new IllegalStateException("Conflicto: Múltiples puertos con el nombre '" + name + "'");
        }
        return ports.isEmpty() ? Optional.empty() : Optional.of(ports.get(0));

    }

    public List<Port> getAllPorts() {
        return portRepository.getAll();
    }
    public void deletePort(String id) {
        portRepository.eliminatePort(id);
    }
}