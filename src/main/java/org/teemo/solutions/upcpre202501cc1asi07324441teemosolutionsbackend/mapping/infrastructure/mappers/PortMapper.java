package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.mappers;

import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents.PortDocument;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.CoordinatesResource;

@Component
public class PortMapper {

    /**
     * Convierte un documento de persistencia (PortDocument) a una entidad de dominio (Port).
     */
    public Port toDomain(PortDocument document) {
        return new Port(
                document.getName(),
                new Coordinates(
                        document.getCoordinates().getLatitude(),
                        document.getCoordinates().getLongitude()
                ),
                document.getContinent()
        );
    }

    /**
     * Convierte una entidad de dominio (Port) a un recurso de API (CoordinatesResource).
     */
    public CoordinatesResource toResource(Port port) {
        return new CoordinatesResource(
                port.getCoordinates().latitude(),
                port.getCoordinates().longitude()
        );
    }
}