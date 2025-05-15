package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.EventDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.EventRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SafetyValidator {

    private final EventRepository eventRepository;

    public List<String> validateRoute(List<Port> route) {
        List<EventDocument> eventos = eventRepository.findAll();

        return route.stream()
                .flatMap(port -> eventos.stream()
                        .filter(evento -> evento.getPuertoOrigen().equalsIgnoreCase(port.getName()))
                        .map(this::formatWarning)
                )
                .toList();
    }

    private String formatWarning(EventDocument evento) {
        return String.format("[%s] %s: %s",
                evento.getEsSegura().equalsIgnoreCase("No segura") ? "ALERTA" : "INFO",
                evento.getPuertoOrigen(),
                evento.getProblemaGeopolitico() != null ?
                        evento.getProblemaGeopolitico() : evento.getEventoMaritimo()
        );
    }
}