package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.mappers;

import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.PortDocument;

@Component
public class PortMapper {

    public Port toDomain(PortDocument doc) {
        Port domain = new Port(
                doc.getName(),
                new Coordinates(doc.getCoordinates().getLatitude(), doc.getCoordinates().getLongitude()),
                doc.getContinent()
        );
        // Transferimos los campos de auditoría y el ID desde el documento al agregado de dominio
        domain.setId(doc.getId());
        domain.setCreatedAt(doc.getCreatedAt());
        domain.setUpdatedAt(doc.getUpdatedAt());
        return domain;
    }

    public PortDocument toDocument(Port domain) {
        PortDocument doc = new PortDocument();
        PortDocument.CoordinatesDocument coordsDoc = new PortDocument.CoordinatesDocument();
        coordsDoc.setLatitude(domain.getCoordinates().latitude());
        coordsDoc.setLongitude(domain.getCoordinates().longitude());

        doc.setId(domain.getId());
        doc.setName(domain.getName());
        doc.setCoordinates(coordsDoc);
        doc.setContinent(domain.getContinent());
        // La auditoría (@CreatedDate, @LastModifiedDate) la manejará Spring Data en el PortDocument.
        // O podemos establecerla aquí si es necesario.
        return doc;
    }
}