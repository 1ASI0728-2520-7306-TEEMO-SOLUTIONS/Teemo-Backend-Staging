package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.exceptions;

public class RouteHistoryNotFoundException extends RuntimeException {
    public RouteHistoryNotFoundException(String historyId) {
        super("Route history entry not found: " + historyId);
    }
}
