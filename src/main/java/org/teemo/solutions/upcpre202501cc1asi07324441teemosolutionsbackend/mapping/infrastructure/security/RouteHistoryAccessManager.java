package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.security;

import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.services.RouteHistoryService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteHistory;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security.RoutingActorContext;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security.RoutingActorContextProvider;

@Component("routeHistoryAccessManager")
public class RouteHistoryAccessManager {

    private final RoutingActorContextProvider actorContextProvider;
    private final RouteHistoryService routeHistoryService;

    public RouteHistoryAccessManager(RoutingActorContextProvider actorContextProvider,
                                     RouteHistoryService routeHistoryService) {
        this.actorContextProvider = actorContextProvider;
        this.routeHistoryService = routeHistoryService;
    }

    public boolean canReadHistory(String targetUserId) {
        RoutingActorContext actor = actorContextProvider.currentActor();
        if (actor.isAnonymous()) {
            return false;
        }
        if (isSelf(actor, targetUserId)) {
            return true;
        }
        return hasOperatorPrivileges(actor);
    }

    public boolean canReadHistoryItem(String historyId) {
        return routeHistoryService.findById(historyId)
                .map(history -> canReadEntry(actorContextProvider.currentActor(), history))
                .orElse(false);
    }

    public boolean canArchiveHistoryItem(String historyId) {
        RoutingActorContext actor = actorContextProvider.currentActor();
        if (!hasOperatorPrivileges(actor)) {
            return false;
        }
        return routeHistoryService.findById(historyId)
                .map(history -> tenantMatches(actor, history.tenantId()))
                .orElse(false);
    }

    private boolean canReadEntry(RoutingActorContext actor, RouteHistory history) {
        if (actor.isAnonymous()) {
            return false;
        }
        if (isSelf(actor, history.userId())) {
            return true;
        }
        return hasOperatorPrivileges(actor) && tenantMatches(actor, history.tenantId());
    }

    private boolean isSelf(RoutingActorContext actor, String targetUserId) {
        return actor.userId() != null && actor.userId().equals(targetUserId);
    }

    private boolean hasOperatorPrivileges(RoutingActorContext actor) {
        return actor.hasRole("ROLE_ADMIN") || actor.hasRole("ROLE_OPERATOR");
    }

    private boolean tenantMatches(RoutingActorContext actor, String historyTenantId) {
        if (actor.tenantId() == null || historyTenantId == null) {
            return true;
        }
        return actor.tenantId().equals(historyTenantId);
    }
}
