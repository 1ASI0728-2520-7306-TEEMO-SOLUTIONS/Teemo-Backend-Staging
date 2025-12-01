package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.infrastructure.persistence.sdmdb.documents.NotificationRecipientDocument;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRecipientRepository extends MongoRepository<NotificationRecipientDocument, String> {

    Optional<NotificationRecipientDocument> findByNotificationIdAndUserId(String notificationId, String userId);

    List<NotificationRecipientDocument> findByNotificationIdInAndUserId(Collection<String> notificationIds, String userId);
}
