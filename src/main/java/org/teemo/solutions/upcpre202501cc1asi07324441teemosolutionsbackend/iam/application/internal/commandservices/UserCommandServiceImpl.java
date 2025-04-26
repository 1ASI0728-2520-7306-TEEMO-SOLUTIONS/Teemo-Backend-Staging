package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.commandservices;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.outboundservices.hashing.HashingService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.outboundservices.tokens.TokenService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.aggregates.User;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.commands.SignInCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.commands.SignUpCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.services.UserCommandService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;


import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;

    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService, TokenService tokenService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
    }


    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username()))
            throw new RuntimeException("Username alredy exists");
        var roles = command.roles().stream().map(
                role -> roleRepository.findByName(role.getName()).
                        orElseThrow(
                                ()-> new RuntimeException("Role name not" +
                                        "found"))).toList();
        var user = new User(command.username(),
                hashingService.encode(command.password()), roles);
        userRepository.save(user);
        return userRepository.findByUsername(command.username());
    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty()) throw new RuntimeException("user not found");
        if (!hashingService.matches(command.password(), user.get().getPassword()))
            throw new RuntimeException("Invalid password");
        var token = tokenService.generateToken(user.get().getUsername());
        return Optional.of(ImmutablePair.of(user.get(), token));
    }
}
