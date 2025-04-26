package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands;

public record CreatePortCommand(
        String name,
        Double latitude,
        Double longitude,
        String continent
) {}
