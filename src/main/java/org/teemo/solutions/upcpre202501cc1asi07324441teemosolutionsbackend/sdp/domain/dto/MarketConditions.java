package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketConditions {
    private String fuelPrices;
    private String exchangeRates;
    private String portCongestion;
    private String seasonalDemand;
}