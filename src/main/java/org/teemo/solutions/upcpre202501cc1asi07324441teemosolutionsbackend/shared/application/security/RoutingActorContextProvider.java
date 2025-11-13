package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoutingActorContextProvider {

    public RoutingActorContext currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return RoutingActorContext.anonymous();
        }

        String userId = extractUserId(authentication.getPrincipal());
        Set<String> roles = authentication.getAuthorities() == null ? Set.of()
                : authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableSet());

        String tenantId = extractTenantId(authentication.getDetails());
        return new RoutingActorContext(userId, tenantId, roles);
    }

    private String extractUserId(Object principal) {
        if (principal instanceof org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return Optional.ofNullable(principal)
                .map(Object::toString)
                .orElse(null);
    }

    private String extractTenantId(Object details) {
        if (details instanceof TenantAware tenantAware) {
            return tenantAware.tenantId();
        }
        return null;
    }

    public interface TenantAware {
        String tenantId();
    }
}
