package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.infrastructure.persistence.sdmdb.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.entities.Notification;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationAction;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationAudience;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationType;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "notifications")
public class NotificationDocument {

    @Id
    private String id;
    private NotificationType type;
    private String title;
    private String message;
    private String portId;
    private String portName;
    private NotificationAction action;
    private String reason;
    private String performedBy;
    private NotificationAudience audience;
    @Indexed
    private Instant createdAt;
    private String targetUserId;

    public Notification toDomain() {
        return new Notification(
                id,
                type,
                title,
                message,
                portId,
                portName,
                action,
                reason,
                performedBy,
                audience,
                createdAt,
                targetUserId
        );
    }
}
