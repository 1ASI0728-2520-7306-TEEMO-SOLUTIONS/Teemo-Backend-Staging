package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.commands.CreatePortCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions.PortNotFoundException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.PortDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories.PortRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PortService {

    private static final Logger logger = LoggerFactory.getLogger(PortService.class);

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
                .map(this::toDocument)
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
        return portRepository.findById(id).map(this::toDomain);
    }

    public boolean existsByNameAndContinent(String name, String continent) {
        return portRepository.existsByNameAndContinent(name, continent);
    }

    public Optional<Port> getPortByName(String name) {
        return portRepository.findByName(name).map(this::toDomain);
    }

    public List<Port> getAllPorts() {
        return portRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    public List<Port> getPortsByDisabled(boolean disabled) {
        return portRepository.findByDisabled(disabled).stream()
                .map(this::toDomain)
                .toList();
    }

    public Port disablePort(String portId, String reason, String actor) {
        PortDocument document = getPortDocumentOrThrow(portId);
        boolean previousState = document.isDisabled();
        Instant now = Instant.now();
        document.setDisabled(true);
        document.setDisabledReason(reason);
        document.setDisabledAt(now);
        document.setDisabledBy(actor);
        PortDocument saved = portRepository.save(document);
        logger.info(
                "AUDIT port.disable actor={} portId={} oldDisabled={} newDisabled={} reason={} timestamp={}",
                actor, portId, previousState, true, reason, now
        );
        return saved.toDomain();
    }

    public Port enablePort(String portId, String actor) {
        PortDocument document = getPortDocumentOrThrow(portId);
        boolean previousState = document.isDisabled();
        String previousReason = document.getDisabledReason();
        Instant now = Instant.now();
        document.setDisabled(false);
        document.setDisabledReason(null);
        document.setDisabledAt(null);
        document.setDisabledBy(null);
        PortDocument saved = portRepository.save(document);
        logger.info(
                "AUDIT port.enable actor={} portId={} oldDisabled={} newDisabled={} clearedReason={} timestamp={}",
                actor, portId, previousState, false, previousReason, now
        );
        return saved.toDomain();
    }

    public void deletePort(String id) {
        portRepository.deleteById(id);
    }

    private PortDocument getPortDocumentOrThrow(String portId) {
        return portRepository.findById(portId)
                .orElseThrow(() -> new PortNotFoundException("Puerto no encontrado: " + portId));
    }

    private Port toDomain(PortDocument document) {
        return new Port(
                document.getId(),
                document.getName(),
                new Coordinates(
                        document.getCoordinates().getLatitude(),
                        document.getCoordinates().getLongitude()
                ),
                document.getContinent(),
                document.isDisabled(),
                document.getDisabledReason(),
                document.getDisabledAt(),
                document.getDisabledBy()
        );
    }

    private PortDocument toDocument(Port port) {
        PortDocument document = new PortDocument(
                port.getName(),
                new PortDocument.CoordinatesDocument(
                        port.getCoordinates().latitude(),
                        port.getCoordinates().longitude()
                ),
                port.getContinent()
        );
        document.setId(port.getId());
        document.setDisabled(port.isDisabled());
        document.setDisabledReason(port.getDisabledReason());
        document.setDisabledAt(port.getDisabledAt());
        document.setDisabledBy(port.getDisabledBy());
        return document;
    }
}
