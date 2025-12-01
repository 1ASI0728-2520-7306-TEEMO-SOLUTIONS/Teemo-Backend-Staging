package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.interfaces.rest.resources;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationAction;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationAudience;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationType;

import java.time.Instant;

public record NotificationResource(
        String id,
        NotificationType type,
        String title,
        String message,
        String portId,
        String portName,
        NotificationAction action,
        String reason,
        String performedBy,
        NotificationAudience audience,
        Instant createdAt,
        boolean read,
        Instant readAt
) {}
