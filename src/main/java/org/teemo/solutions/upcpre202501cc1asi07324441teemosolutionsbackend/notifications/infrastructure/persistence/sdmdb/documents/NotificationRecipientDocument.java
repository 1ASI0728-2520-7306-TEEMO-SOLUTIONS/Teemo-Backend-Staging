package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.infrastructure.persistence.sdmdb.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "notification_recipients")
@CompoundIndex(name = "idx_notification_user", def = "{'notificationId': 1, 'userId': 1}", unique = true)
public class NotificationRecipientDocument {

    @Id
    private String id;
    private String notificationId;
    private String userId;
    private Instant readAt;
    private Instant createdAt;
}
