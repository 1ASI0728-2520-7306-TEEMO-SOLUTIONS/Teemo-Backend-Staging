package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncotermOption {
    private String code;
    private String name;
    private String description;
    private String riskTransferPoint;
    private List<String> suitableFor;
    private List<String> considerations;
    private List<String> sellerResponsibilities;
    private List<String> buyerResponsibilities;
    private int recommendationScore;
    private CostBreakdown costBreakdown;
}
