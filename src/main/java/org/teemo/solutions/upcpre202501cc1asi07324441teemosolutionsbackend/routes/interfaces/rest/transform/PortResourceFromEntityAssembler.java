package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources.PortResource;

public class PortResourceFromEntityAssembler {
    public static PortResource toResourceFromEntity(Port port) {
        return new PortResource(
                port.getId(),
                port.getName(),
                port.getLatitude(),
                port.getLongitude(),
                port.getContinent()
        );
    }
}
