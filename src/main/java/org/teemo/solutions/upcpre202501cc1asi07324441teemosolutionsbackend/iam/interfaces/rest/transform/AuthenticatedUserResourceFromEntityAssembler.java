package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.aggregates.User;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user,
                                                                 String token) {
        return new AuthenticatedUserResource(user.getId(), user.getUsername(), token);
    }
}
