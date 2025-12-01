package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.application.internal.querymodels;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.entities.Notification;

import java.time.Instant;

public record NotificationView(Notification notification, Instant readAt) {

    public boolean isRead() {
        return readAt != null;
    }
}
