package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreateMapCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.interfaces.rest.resources.CreateMapResource;

public class CreateMapCommandFromResourceAssembler {
    public static CreateMapCommand toCommandFromResource(CreateMapResource resource) {
        return new CreateMapCommand(resource.name());
    }
}
