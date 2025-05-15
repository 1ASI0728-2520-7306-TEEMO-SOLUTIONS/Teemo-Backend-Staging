package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.entities;

import java.time.LocalDateTime;
import java.util.List;

// ErrorResponse.java
public record ErrorResponse(
        String message,
        LocalDateTime timestamp,
        List<String> details
) {
    public ErrorResponse(String message, LocalDateTime timestamp) {
        this(message, timestamp, null);
    }
}
