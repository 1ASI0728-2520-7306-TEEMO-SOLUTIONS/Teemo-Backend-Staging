package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.commands.ConfirmUserRegistrationCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources.ConfirmRegistrationResource;

public class ConfirmUserRegistrationCommandFromResourceAssembler {

    public static ConfirmUserRegistrationCommand toCommandFromResource(ConfirmRegistrationResource resource) {
        return new ConfirmUserRegistrationCommand(resource.token());
    }
}