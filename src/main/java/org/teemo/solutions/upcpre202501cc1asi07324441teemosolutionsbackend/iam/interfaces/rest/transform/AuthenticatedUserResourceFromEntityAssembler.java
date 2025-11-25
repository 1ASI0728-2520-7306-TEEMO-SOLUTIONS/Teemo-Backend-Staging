package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.aggregates.User;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.entities.Role;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources.AuthenticatedUserResource;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user,
                                                                 String token) {
        Collection<Role> assignedRoles = user.getRoles() != null ? user.getRoles() : List.of();
        List<String> roles = assignedRoles.stream()
                .map(Role::getStringName)
                .toList();
        return new AuthenticatedUserResource(user.getId(), user.getUsername(), token, roles);
    }
}
