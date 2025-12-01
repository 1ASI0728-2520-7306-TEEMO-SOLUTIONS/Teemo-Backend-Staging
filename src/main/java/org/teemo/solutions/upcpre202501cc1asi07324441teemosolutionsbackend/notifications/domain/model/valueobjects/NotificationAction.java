package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.domain.model.valueobjects;

/**
 * Action performed over a port that should fan-out as a notification.
 */
public enum NotificationAction {
    DISABLED,
    ENABLED;

    public boolean isDisabled() {
        return this == DISABLED;
    }
}
