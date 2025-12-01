package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.interfaces.rest.resources;

import java.util.List;

public record NotificationPageResource(
        List<NotificationResource> items,
        long totalItems,
        int totalPages,
        int page,
        int size,
        boolean hasNext
) {}
