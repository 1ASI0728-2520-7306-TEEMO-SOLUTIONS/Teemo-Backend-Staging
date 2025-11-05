package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.sdp.presentation.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncotermFormDataRequest {
    @NotBlank
    private String cargoType;

    @Positive
    private double cargoValue;

    @Positive
    private double cargoWeight;

    @Positive
    private double cargoVolume;

    @NotBlank
    private String seller;

    @NotBlank
    private String buyer;

    @NotBlank
    private String sellerCountry;

    @NotBlank
    private String buyerCountry;

    @NotBlank
    private String paymentTerms;

    @NotBlank
    private String experienceLevel;

    private boolean insurance;

    private String specialRequirements;

    @NotBlank
    private String originPort;

    @NotBlank
    private String destinationPort;

    @Positive
    private double distance;
}