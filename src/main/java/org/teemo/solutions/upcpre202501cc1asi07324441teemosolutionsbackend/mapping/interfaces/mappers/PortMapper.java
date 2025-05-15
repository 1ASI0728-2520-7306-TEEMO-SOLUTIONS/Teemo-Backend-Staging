package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.mappers;

import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.dtos.PortDto;

@Component
public class PortMapper {
    public PortDto toDto(Port port) {
        return new PortDto(
                port.getName(),
                port.getCoordinates().latitude(),
                port.getCoordinates().longitude(),
                port.getContinent()
        );
    }
}