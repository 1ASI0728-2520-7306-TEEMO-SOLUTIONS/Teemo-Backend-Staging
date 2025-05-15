package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Getter
@Setter
@Document(collection = "events-documents")
public class EventDocument extends AuditableAbstractAggregateRoot<EventDocument> {
    private String id;
    private String puertoOrigen; // Mapea "Puerto Origen"
    private String problemaGeopolitico; // Mapea "Problema Geopolítico"
    private String eventoMaritimo; // Para "Evento Marítimo"
    private String esSegura; // Para "¿Es segura o no segura?"
}