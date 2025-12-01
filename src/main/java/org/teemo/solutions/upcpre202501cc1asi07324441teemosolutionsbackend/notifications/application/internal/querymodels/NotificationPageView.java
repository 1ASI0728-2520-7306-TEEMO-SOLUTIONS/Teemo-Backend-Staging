package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.application.internal.querymodels;

import java.util.List;

public record NotificationPageView(List<NotificationView> items,
                                   long totalElements,
                                   int totalPages,
                                   int pageNumber,
                                   int pageSize) {

    public boolean hasNext() {
        return totalPages > 0 && pageNumber + 1 < totalPages;
    }
}
