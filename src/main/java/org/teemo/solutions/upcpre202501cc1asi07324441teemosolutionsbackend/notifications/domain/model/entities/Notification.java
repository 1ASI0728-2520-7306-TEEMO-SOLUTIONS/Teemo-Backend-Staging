package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.entities;

import lombok.Getter;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationAction;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationAudience;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationType;

import java.time.Instant;
import java.util.Objects;

@Getter
public class Notification {

    private final String id;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final String portId;
    private final String portName;
    private final NotificationAction action;
    private final String reason;
    private final String performedBy;
    private final NotificationAudience audience;
    private final Instant createdAt;
    private final String targetUserId;

    public Notification(String id, NotificationType type, String title, String message, String portId,
                        String portName, NotificationAction action, String reason, String performedBy,
                        NotificationAudience audience, Instant createdAt, String targetUserId) {
        this.id = Objects.requireNonNull(id, "id");
        this.type = Objects.requireNonNull(type, "type");
        this.title = Objects.requireNonNull(title, "title");
        this.message = Objects.requireNonNull(message, "message");
        this.portId = portId;
        this.portName = portName;
        this.action = Objects.requireNonNull(action, "action");
        this.reason = reason;
        this.performedBy = performedBy;
        this.audience = Objects.requireNonNull(audience, "audience");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.targetUserId = targetUserId;
    }
}
