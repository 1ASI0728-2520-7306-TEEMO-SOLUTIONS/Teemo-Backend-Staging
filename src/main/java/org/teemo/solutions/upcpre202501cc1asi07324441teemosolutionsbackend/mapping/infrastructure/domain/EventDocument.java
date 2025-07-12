package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Getter
@Setter
@Document(collection = "events-documents")
public class EventDocument extends AuditableAbstractAggregateRoot<EventDocument> {

    @Field("puertoOrigen")
    private String puertoOrigen;

    // Corregido: eliminado el "\n"
    @Field("problemaGeopolitico\n")
    private String problemaGeopolitico;

    @Field("descripcion")
    private String descripcion;

    // Metodo seguro para acceso a puertoOrigen
    public String getSafeOriginPort() {
        return (puertoOrigen != null && !puertoOrigen.isBlank()) ?
                puertoOrigen : "Origen Desconocido";
    }
}