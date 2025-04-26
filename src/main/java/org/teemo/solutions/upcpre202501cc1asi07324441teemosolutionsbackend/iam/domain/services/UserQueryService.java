package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.aggregates.User;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.queries.GetAllUsersQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.queries.GetUserByIdQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.queries.GetUserByUsernameQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {
    List<User> handle(GetAllUsersQuery query);
    Optional<User> handle(GetUserByIdQuery query);
    Optional<User> handle(GetUserByUsernameQuery query);
}
