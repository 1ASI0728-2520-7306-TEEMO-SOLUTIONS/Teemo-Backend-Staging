package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.dtos;

import jakarta.validation.constraints.NotBlank;

public record RouteRequest(
        @NotBlank(message = "El puerto de origen es obligatorio")
        String startPort,

        @NotBlank(message = "El puerto de destino es obligatorio")
        String endPort
) {}