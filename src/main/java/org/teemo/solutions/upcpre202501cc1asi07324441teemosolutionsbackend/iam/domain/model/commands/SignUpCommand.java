package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.commands;

import jakarta.validation.constraints.NotNull;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.entities.Role;

import java.util.List;

public record SignUpCommand(
    String username,
    String password,
    String email,
    List<Role> roles) {
}
