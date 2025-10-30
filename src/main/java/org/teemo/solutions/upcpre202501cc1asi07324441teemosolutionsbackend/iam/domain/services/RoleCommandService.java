package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}
