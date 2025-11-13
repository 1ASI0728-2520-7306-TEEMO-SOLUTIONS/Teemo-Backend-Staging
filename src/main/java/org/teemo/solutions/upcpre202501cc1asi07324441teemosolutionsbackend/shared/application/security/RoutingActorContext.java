package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public record RoutingActorContext(String userId, String tenantId, Set<String> roles) {

    public static RoutingActorContext anonymous() {
        return new RoutingActorContext(null, null, Set.of());
    }

    public boolean isAnonymous() {
        return userId == null;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public Set<String> roles() {
        return Collections.unmodifiableSet(Objects.requireNonNullElse(roles, Set.of()));
    }
}
