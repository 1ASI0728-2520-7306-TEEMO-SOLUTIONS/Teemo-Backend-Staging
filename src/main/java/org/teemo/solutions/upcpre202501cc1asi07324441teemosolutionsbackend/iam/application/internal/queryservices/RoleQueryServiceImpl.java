package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.entities.Role;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.queries.GetAllRolesQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.queries.GetRoleByNameQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.services.RoleQueryService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RoleQueryServiceImpl  implements RoleQueryService {
    private final RoleRepository roleRepository;

    public RoleQueryServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> handle(GetAllRolesQuery query) {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> handle(GetRoleByNameQuery query) {
        return roleRepository.findByName(query.name());
    }
}
