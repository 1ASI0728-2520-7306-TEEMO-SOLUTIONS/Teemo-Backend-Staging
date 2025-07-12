// SafetyValidator.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices;

import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.SafetyValidator;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.EventDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.EventRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SafetyValidatorImpl implements SafetyValidator {

    private final EventRepository eventRepository;

    public SafetyValidatorImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Set<String> getUnsafePortNames() {
        // Consulta la BD UNA SOLA VEZ, procesa los datos y devuelve un Set simple.
        return eventRepository.findValidEvents().stream()
                .filter(this::isValidEvent)
                .map(EventDocument::getSafeOriginPort) // Extrae el nombre del puerto del evento
                .filter(Objects::nonNull) // Asegura que no haya nulos
                .collect(Collectors.toSet());
    }

    @Override
    public List<String> validateFullRoute(List<Port> route) {
        // Esta función puede seguir usando la lógica original si es necesario,
        // pero ahora puede ser más eficiente usando el Set.
        Set<String> unsafePorts = getUnsafePortNames();
        return route.stream()
                .map(Port::getName)
                .filter(unsafePorts::contains)
                .map(portName -> "[ALERTA] El puerto " + portName + " está afectado por inestabilidad geopolítica.")
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isValidEvent(EventDocument event) {
        return event.getPuertoOrigen() != null && !event.getPuertoOrigen().isBlank();
    }
}