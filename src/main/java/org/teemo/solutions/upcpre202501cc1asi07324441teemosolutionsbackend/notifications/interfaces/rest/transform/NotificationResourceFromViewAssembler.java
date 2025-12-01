package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.interfaces.rest.transform;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.application.internal.querymodels.NotificationPageView;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.application.internal.querymodels.NotificationView;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.entities.Notification;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.interfaces.rest.resources.NotificationPageResource;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.interfaces.rest.resources.NotificationResource;

import java.util.List;

public class NotificationResourceFromViewAssembler {

    private NotificationResourceFromViewAssembler() {
    }

    public static NotificationPageResource toPageResource(NotificationPageView page) {
        List<NotificationResource> resources = page.items().stream()
                .map(NotificationResourceFromViewAssembler::toResource)
                .toList();
        return new NotificationPageResource(
                resources,
                page.totalElements(),
                page.totalPages(),
                page.pageNumber(),
                page.pageSize(),
                page.hasNext()
        );
    }

    private static NotificationResource toResource(NotificationView view) {
        Notification notification = view.notification();
        return new NotificationResource(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getPortId(),
                notification.getPortName(),
                notification.getAction(),
                notification.getReason(),
                notification.getPerformedBy(),
                notification.getAudience(),
                notification.getCreatedAt(),
                view.isRead(),
                view.readAt()
        );
    }
}
