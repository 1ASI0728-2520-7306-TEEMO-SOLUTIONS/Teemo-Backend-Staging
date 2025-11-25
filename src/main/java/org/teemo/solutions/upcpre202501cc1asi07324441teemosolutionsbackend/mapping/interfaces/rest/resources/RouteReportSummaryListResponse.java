package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.interfaces.rest.resources;

import java.util.List;

public record RouteReportSummaryListResponse(
        List<RouteReportSummaryResource> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
