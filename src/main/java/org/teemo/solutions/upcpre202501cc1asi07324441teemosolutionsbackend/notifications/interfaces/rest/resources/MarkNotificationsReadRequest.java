package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.notifications.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MarkNotificationsReadRequest(
        @NotEmpty(message = "Debe especificar al menos una notificación")
        List<@NotBlank String> ids
) {}
