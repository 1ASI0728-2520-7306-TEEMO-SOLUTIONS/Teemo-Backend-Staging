package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.CoordinatesResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.PortResource;

public class PortResourceFromEntityAssembler {
    public static PortResource toResourceFromEntity(Port port) {
        return new PortResource(
                port.getId(),
                port.getName(),
                new CoordinatesResource(
                        port.getCoordinates().latitude(),
                        port.getCoordinates().longitude()
                ),
                port.getContinent()
        );
    }
}