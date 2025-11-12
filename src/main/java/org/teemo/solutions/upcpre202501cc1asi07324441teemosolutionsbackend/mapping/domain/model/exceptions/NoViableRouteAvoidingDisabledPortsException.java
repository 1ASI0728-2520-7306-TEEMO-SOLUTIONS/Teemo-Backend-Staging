package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions;

import java.util.Set;

public class NoViableRouteAvoidingDisabledPortsException extends RuntimeException {

    private final Set<String> avoidedPortIds;

    public NoViableRouteAvoidingDisabledPortsException(String message, Set<String> avoidedPortIds) {
        super(message);
        this.avoidedPortIds = avoidedPortIds == null ? Set.of() : Set.copyOf(avoidedPortIds);
    }

    public Set<String> getAvoidedPortIds() {
        return avoidedPortIds;
    }
}
