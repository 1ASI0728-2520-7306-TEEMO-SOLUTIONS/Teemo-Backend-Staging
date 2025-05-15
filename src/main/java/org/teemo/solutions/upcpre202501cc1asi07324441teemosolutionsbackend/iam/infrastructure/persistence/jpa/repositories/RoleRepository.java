package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.persistence.jpa.repositories;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.entities.Role;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.valueobjects.Roles;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(Roles name);
    List<Role> findAll();
    void saveRole(Role role);
    boolean existsByName(Roles name);
}