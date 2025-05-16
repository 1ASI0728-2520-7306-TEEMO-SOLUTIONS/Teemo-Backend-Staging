// SafetyValidator.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates;

import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.EventDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.EventRepository;

import java.util.List;

@Component
public class SafetyValidator {

    private final EventRepository eventRepository;

    public SafetyValidator(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // SafetyValidator.java - Corrección del Stream
    public List<String> validateRoute(List<Port> route) {
        return eventRepository.findValidEvents().stream()
                .filter(this::isValidEvent)
                .flatMap(event -> route.stream()
                        .filter(port -> isAffectedPort(port, event))
                        .map(port -> buildWarningMessage(event))
                )
                .toList(); // Añadir .toList() aquí
    }
    private boolean isValidEvent(EventDocument event) {
        return event.getPuertoOrigen() != null &&
                !event.getPuertoOrigen().isBlank();
    }

    private boolean isAffectedPort(Port port, EventDocument event) {
        return port.getName().equalsIgnoreCase(event.getSafePuertoOrigen());
    }

    private String buildWarningMessage(EventDocument event) {
        String tipo = "ALERTA"; // Todos los eventos en tu DB son de tipo alerta
        return String.format("[%s] %s: %s",
                tipo,
                event.getSafePuertoOrigen(),
                getEventMessage(event));
    }

    private String getEventMessage(EventDocument event) {
        return event.getProblemaGeopolitico() != null ?
                event.getProblemaGeopolitico() :
                event.getDescripcion();
    }
}