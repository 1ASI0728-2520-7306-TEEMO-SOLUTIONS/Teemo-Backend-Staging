package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.dtos;

public record PortDto(
        String name,
        double latitude,
        double longitude,
        String continent
) {}