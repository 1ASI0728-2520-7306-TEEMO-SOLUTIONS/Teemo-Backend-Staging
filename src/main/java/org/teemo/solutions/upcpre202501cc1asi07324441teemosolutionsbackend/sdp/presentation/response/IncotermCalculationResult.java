package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.presentation.response;

import lombok.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.dto.CostComparison;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.dto.IncotermOption;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.dto.MarketConditions;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.dto.RouteDetails;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncotermCalculationResult {
    private MarketConditions marketConditions;
    private IncotermOption recommendedIncoterm;
    private CostComparison costComparison;
    private List<IncotermOption> alternatives;
    private RouteDetails routeDetails;
    private List<String> warnings;
}