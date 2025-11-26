package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects;

public enum PortOperationalStatus {
    OPEN,
    RESTRICTED,
    CLOSED;

    public static PortOperationalStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return PortOperationalStatus.valueOf(value.toUpperCase());
    }
}
