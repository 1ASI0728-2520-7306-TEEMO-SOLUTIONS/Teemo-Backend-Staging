package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.commands.CreatePortCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.PortDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PortService {

    private final PortRepository portRepository;

    @Autowired
    public PortService (PortRepository portRepository) {
        this.portRepository = portRepository;
    }

    public Port createPort(CreatePortCommand command) {
        PortDocument port = new PortDocument(
                command.name(),
                new PortDocument.CoordinatesDocument(command.coordinates().latitude(), command.coordinates().longitude()),
                command.continent()
        );
        return portRepository.save(port).toDomain();
    }

    public void saveAllPorts(List<Port> ports) {
        List<PortDocument> portDocuments = ports.stream()
                .map(port -> new PortDocument(
                        port.getName(),
                        new PortDocument.CoordinatesDocument(
                                port.getCoordinates().latitude(),
                                port.getCoordinates().longitude()
                        ),
                        port.getContinent()
                ))
                .toList();
        portRepository.saveAll(portDocuments);
    }

    public Optional<PortDocument> findByNameAndContinent(String name, String continent) {
        return portRepository.findByNameAndContinent(name, continent);
    }

    public void deleteAllPorts() {
        portRepository.deleteAll();
    }

    public Optional<Port> getPortById(String id) {
        return portRepository.findById(id).map(portDocument -> new Port(
                portDocument.getName(),
                new Coordinates(
                        portDocument.getCoordinates().getLatitude(),
                        portDocument.getCoordinates().getLongitude()
                ),
                portDocument.getContinent()
        ));
    }

    public boolean existsByNameAndContinent(String name, String continent) {
        return portRepository.existsByNameAndContinent(name, continent);
    }

    public Optional<Port> getPortByName(String name) {
        return portRepository.findByName(name).map(portDocument -> new Port(
                portDocument.getName(),
                new Coordinates(
                        portDocument.getCoordinates().getLatitude(),
                        portDocument.getCoordinates().getLongitude()
                ),
                portDocument.getContinent()
        ));
    }

    public List<Port> getAllPorts() {
        return portRepository.findAll().stream()
                .map(portDocument -> new Port(
                        portDocument.getId(),
                        portDocument.getName(),
                        new Coordinates(
                                portDocument.getCoordinates().getLatitude(),
                                portDocument.getCoordinates().getLongitude()
                        ),
                        portDocument.getContinent()
                ))
                .toList();
    }

    public void deletePort(String id) {
        portRepository.deleteById(id);
    }
}