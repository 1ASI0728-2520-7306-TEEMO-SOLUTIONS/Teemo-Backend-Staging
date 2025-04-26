package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.commands.SignInCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.username(), resource.password());
    }
}
