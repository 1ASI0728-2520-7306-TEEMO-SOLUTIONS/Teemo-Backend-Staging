package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.application.internal.services;

import org.springframework.data.domain.Pageable;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.application.internal.querymodels.NotificationPageView;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.entities.Notification;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects.NotificationAction;

import java.util.Collection;

public interface NotificationService {

    Notification registerPortStatusChange(Port port, NotificationAction action, String reason, String performedBy);

    NotificationPageView getNotificationsForUser(String userId, Pageable pageable);

    void markNotificationAsRead(String notificationId, String userId);

    void markNotificationsAsRead(Collection<String> notificationIds, String userId);
}
