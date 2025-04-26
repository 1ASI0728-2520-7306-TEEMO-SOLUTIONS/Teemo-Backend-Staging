package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreatePortCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources.CreatePortResource;

public class CreatePortCommandFromResourceAssembler {
    public static CreatePortCommand toCommandFromResource(CreatePortResource resource) {
        return new CreatePortCommand(
                resource.name(),
                resource.latitude(),
                resource.longitude(),
                resource.continent()
        );
    }
}
