package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.commands.CreatePortCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources.CreatePortResource;

public class CreatePortCommandFromResourceAssembler {
    public static CreatePortCommand toCommandFromResource(CreatePortResource resource) {
        return new CreatePortCommand(
                resource.name(),
                new Coordinates(
                        resource.coordinates().latitude(),
                        resource.coordinates().longitude()
                ),
                resource.continent()
        );
    }
}