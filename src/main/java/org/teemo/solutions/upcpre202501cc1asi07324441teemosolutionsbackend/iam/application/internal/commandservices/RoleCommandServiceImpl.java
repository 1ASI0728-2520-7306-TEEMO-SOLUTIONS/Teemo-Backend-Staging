package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.commandservices;


import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.commands.SeedRolesCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.entities.Role;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.valueobjects.Roles;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.services.RoleCommandService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;

import java.util.Arrays;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {
    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void handle(SeedRolesCommand command) {
        Arrays.stream(Roles.values()).forEach(
                role -> {
                    if (!roleRepository.existsByName(role)) {
                        roleRepository.save(new Role(Roles.valueOf(role.name())));
                    }
                }
        );
    }
}