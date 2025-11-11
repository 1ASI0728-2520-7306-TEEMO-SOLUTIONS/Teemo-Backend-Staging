package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "HazardAssessmentRequest", description = "Entrada para evaluar probabilidad de eventos de riesgo en la ruta")
public class HazardAssessmentRequest {

    @Schema(example = "-12.0564", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double originLat;

    @Schema(example = "-77.1319", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double originLon;

    @Schema(example = "38.7223", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double destLat;

    @Schema(example = "-9.1393", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double destLon;

    @Schema(example = "2025-11-01T10:00:00Z", description = "Fecha/hora de zarpe en ISO-8601 (Z/offset). Si no se envía, se usa ahora.")
    private String departureTimeIso;

    @Schema(example = "18", description = "Velocidad de crucero en nudos (opcional, solo para calcular plannedHours).")
    private Double cruiseSpeedKnots;

    // getters / setters
    public Double getOriginLat() { return originLat; }
    public void setOriginLat(Double originLat) { this.originLat = originLat; }
    public Double getOriginLon() { return originLon; }
    public void setOriginLon(Double originLon) { this.originLon = originLon; }
    public Double getDestLat() { return destLat; }
    public void setDestLat(Double destLat) { this.destLat = destLat; }
    public Double getDestLon() { return destLon; }
    public void setDestLon(Double destLon) { this.destLon = destLon; }
    public String getDepartureTimeIso() { return departureTimeIso; }
    public void setDepartureTimeIso(String departureTimeIso) { this.departureTimeIso = departureTimeIso; }
    public Double getCruiseSpeedKnots() { return cruiseSpeedKnots; }
    public void setCruiseSpeedKnots(Double cruiseSpeedKnots) { this.cruiseSpeedKnots = cruiseSpeedKnots; }
}
