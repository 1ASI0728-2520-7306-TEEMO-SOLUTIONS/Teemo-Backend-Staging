package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.domain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostBreakdown {
    public double oceanFreight;
    public double fuelSurcharge;
    public double bunkerAdjustmentFactor;
    public double originPortHandling;
    public double destinationPortHandling;
    public double containerHandling;
    public double stevedoring;
    public double marineInsurance;
    public double cargoInsurance;
    public double warRiskInsurance;
    public double exportCustomsClearance;
    public double importCustomsClearance;
    public double importDuties;
    public double taxes;
    public double billOfLading;
    public double certificateOfOrigin;
    public double inspectionCertificate;
    public double customsDocumentation;
    public double warehousing;
    public double demurrage;
    public double detention;
    public double currencyAdjustment;

    public double sellerCosts;
    public double buyerCosts;
    public double total;
}