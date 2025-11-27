package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.commandservices;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.outboundservices.email.EmailService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.outboundservices.hashing.HashingService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.outboundservices.tokens.TokenService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.aggregates.User;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.commands.ConfirmUserRegistrationCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.commands.SignInCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.commands.SignUpCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.domain.model.valueobjects.Roles;
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
    private final EmailService emailService;

    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService, TokenService tokenService, RoleRepository roleRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new RuntimeException("Username already exists");
        }

        var roles = command.roles();
        if (roles.isEmpty()) {
            var role = roleRepository.findByName(Roles.ROLE_USER);
            if (role.isEmpty()) {
                throw new RuntimeException("Role not found. Check the role name.");
            }

            roles.add(role.get());
        }
        roles = command.roles()
                .stream()
                .map(role -> roleRepository.findByName(role.getName())
                        .orElseThrow(() -> new RuntimeException("Role not found"))).toList();
        var user = new User(command.username(), hashingService.encode(command.password()), command.email(), roles);
        user.setRegistered(false);
        userRepository.saveUser(user);

        var token = tokenService.generateToken(user.getUsername());
        var confirmationLink = "http://localhost:8080/api/authentication/confirm?token=" + token;

        var subject = "Por favor, confirme su registro";
        var body = "Por favor, confirme su registro haciendo clic en el siguiente enlace:\n\n"
                + confirmationLink + "\n\n"
                + "Si usted no creó esta cuenta, puede ignorar este mensaje.";

        emailService.sendEmail(user.getEmail(), subject, body);

        return userRepository.findByUsername(command.username());
    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var userOpt  = userRepository.findByUsername(command.username());
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");

        var user = userOpt.get();
        if (!hashingService.matches(command.password(), user.getPassword()))
            throw new RuntimeException("Invalid password");
        if (!user.isRegistered()) {
            throw new RuntimeException("User is not confirmed");
        }

        var token = tokenService.generateToken(user.getUsername());
        return Optional.of(ImmutablePair.of(user, token));
    }

    @Override
    public Optional<User> handle(ConfirmUserRegistrationCommand command) {
        var token = command.token();

        if (!tokenService.validateToken(token)) {
            throw new RuntimeException("Invalid confirmation token");
        }

        var username = tokenService.getUsernameFromToken(token);
        var userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        var user = userOpt.get();
        user.confirmRegistration();        // <- cambia isRegistered a true
        userRepository.saveUser(user);

        return Optional.of(user);
    }
}