package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.queries.GetAllUsersQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.queries.GetUserByIdQuery;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.services.UserQueryService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources.UserResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UsersController {
    private final UserQueryService userQueryService;

    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var getAllUsersQuery = new GetAllUsersQuery();
        var users = userQueryService.handle(getAllUsersQuery);
        var userResources = users.stream().map(
                UserResourceFromEntityAssembler::toResourceFromEntity
        ).toList();
        return ResponseEntity.ok(userResources);
    }

    @GetMapping(value= "/{userId}")
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }
}
