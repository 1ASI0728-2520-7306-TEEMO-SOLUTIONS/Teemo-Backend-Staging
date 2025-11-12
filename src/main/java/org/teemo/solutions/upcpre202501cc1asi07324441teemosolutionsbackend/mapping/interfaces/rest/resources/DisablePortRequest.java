package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

import jakarta.validation.constraints.Size;

public record DisablePortRequest(
        @Size(max = 512, message = "La razón no puede exceder 512 caracteres")
        String reason
) {}
