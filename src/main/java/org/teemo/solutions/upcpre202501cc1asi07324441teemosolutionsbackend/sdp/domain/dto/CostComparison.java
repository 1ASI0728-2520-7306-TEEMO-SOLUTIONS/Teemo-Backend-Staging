package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostComparison {
    private String lowestCost;
    private String highestCost;
    private double costDifference;
}
