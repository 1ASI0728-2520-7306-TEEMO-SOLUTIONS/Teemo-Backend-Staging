package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.application.internal.services.NotificationService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.interfaces.rest.resources.MarkNotificationsReadRequest;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.interfaces.rest.resources.NotificationPageResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.interfaces.rest.transform.NotificationResourceFromViewAssembler;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security.RoutingActorContext;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.application.security.RoutingActorContextProvider;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping(value = "/api/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Notifications Endpoints")
public class NotificationController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final NotificationService notificationService;
    private final RoutingActorContextProvider actorContextProvider;

    public NotificationController(NotificationService notificationService,
                                  RoutingActorContextProvider actorContextProvider) {
        this.notificationService = notificationService;
        this.actorContextProvider = actorContextProvider;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR','ROLE_VIEWER')")
    public ResponseEntity<NotificationPageResource> getNotifications(@RequestParam(name = "page", defaultValue = "0") int page,
                                                                     @RequestParam(name = "limit", defaultValue = "" + DEFAULT_LIMIT) int limit,
                                                                     @RequestParam(name = "order", defaultValue = "desc") String order) {
        String userId = requireUser();
        Pageable pageable = PageRequest.of(
                normalizePage(page),
                normalizeLimit(limit),
                Sort.by(resolveDirection(order), "createdAt")
        );
        var pageView = notificationService.getNotificationsForUser(userId, pageable);
        return ResponseEntity.ok(NotificationResourceFromViewAssembler.toPageResource(pageView));
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR','ROLE_VIEWER')")
    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {
        String userId = requireUser();
        notificationService.markNotificationAsRead(notificationId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/read", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_OPERATOR','ROLE_VIEWER')")
    public ResponseEntity<Void> markAllAsRead(@Valid @RequestBody MarkNotificationsReadRequest request) {
        String userId = requireUser();
        notificationService.markNotificationsAsRead(request.ids(), userId);
        return ResponseEntity.noContent().build();
    }

    private String requireUser() {
        RoutingActorContext actor = actorContextProvider.currentActor();
        if (actor == null || actor.isAnonymous() || actor.userId() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Usuario no autenticado");
        }
        return actor.userId();
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeLimit(int limit) {
        if (limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private Sort.Direction resolveDirection(String order) {
        return "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
