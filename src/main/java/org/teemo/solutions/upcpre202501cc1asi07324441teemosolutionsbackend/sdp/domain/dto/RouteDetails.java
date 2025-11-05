package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteDetails {
    private double distance;
    private String estimatedTime;
    private String routeComplexity;
    private String riskLevel;
    private List<String> seasonalFactors;
}
