package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.services.UserCommandService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources.SignInResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources.SignUpResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.resources.UserResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {
    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource resource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var authenticatedUser = userCommandService.handle(signInCommand);
        if (authenticatedUser.isEmpty()) return ResponseEntity.notFound().build();
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(
                authenticatedUser.get().getLeft(), authenticatedUser.get().getRight()
        );
        return ResponseEntity.ok(authenticatedUserResource);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserResource> signIn(@RequestBody SignUpResource resource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(
                user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }
}
