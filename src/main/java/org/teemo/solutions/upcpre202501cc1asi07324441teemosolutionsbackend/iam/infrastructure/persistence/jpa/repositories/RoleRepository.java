package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.entities.Role;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.valueobjects.Roles;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(Roles name);
    boolean existsByName(Roles name);

}