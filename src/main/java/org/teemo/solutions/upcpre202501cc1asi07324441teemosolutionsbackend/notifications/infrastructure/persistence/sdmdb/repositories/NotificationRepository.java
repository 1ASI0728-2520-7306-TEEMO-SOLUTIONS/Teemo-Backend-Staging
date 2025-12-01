package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.infrastructure.persistence.sdmdb.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationAudience;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.infrastructure.persistence.sdmdb.documents.NotificationDocument;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationDocument, String> {

    Page<NotificationDocument> findByAudience(NotificationAudience audience, Pageable pageable);
}
