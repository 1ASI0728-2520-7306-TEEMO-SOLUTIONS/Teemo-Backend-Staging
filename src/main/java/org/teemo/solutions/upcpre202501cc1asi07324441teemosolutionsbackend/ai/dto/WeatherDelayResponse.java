package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "WeatherDelayResponse", description = "Resultado de la predicción")
public class WeatherDelayResponse {
    @Schema(example = "6.8")
    private double delayHours;

    @Schema(example = "0.58", description = "0..1")
    private double delayProbability;

    @Schema(example = "2025-10-03T06:00:00Z", nullable = true)
    private String plannedEtaIso;

    @Schema(example = "2025-10-03T12:48:00Z", nullable = true)
    private String adjustedEtaIso;

    @Schema(example = "Viento fuerte")
    private String mainDelayFactor;

    @Schema(example = "false")
    private boolean usedFallback;

    // 🔹 Diagnóstico (ya existentes)
    @Schema(example = "21.4", description = "Viento promedio usado por el modelo (knots)", nullable = true)
    private Double usedAvgWindKnots;

    @Schema(example = "3.6", description = "Altura máxima de ola usada por el modelo (m)", nullable = true)
    private Double usedMaxWaveM;

    // 🔹 Nuevos: solo informativos (no alteran delayHours)
    @Schema(example = "true", description = "Indica si la ruta es viable según reglas de negocio (p.ej., hielo)")
    private Boolean routeViable;

    @Schema(example = "Hielo estacional", nullable = true, description = "Razón de no viabilidad si routeViable=false")
    private String nonViableReason;

    @Schema(example = "0.31", nullable = true, description = "Índice informativo [0..1] compuesto de riesgos a lo largo de la ruta")
    private Double overallHazardProbability;

    @Schema(description = "Lista de probabilidades por tipo de evento y zona")
    private List<WeatherHazardProbability> hazards;

    // ---------------- Getters / Setters ----------------
    public double getDelayHours() { return delayHours; }
    public void setDelayHours(double delayHours) { this.delayHours = delayHours; }

    public double getDelayProbability() { return delayProbability; }
    public void setDelayProbability(double delayProbability) { this.delayProbability = delayProbability; }

    public String getPlannedEtaIso() { return plannedEtaIso; }
    public void setPlannedEtaIso(String plannedEtaIso) { this.plannedEtaIso = plannedEtaIso; }

    public String getAdjustedEtaIso() { return adjustedEtaIso; }
    public void setAdjustedEtaIso(String adjustedEtaIso) { this.adjustedEtaIso = adjustedEtaIso; }

    public String getMainDelayFactor() { return mainDelayFactor; }
    public void setMainDelayFactor(String mainDelayFactor) { this.mainDelayFactor = mainDelayFactor; }

    public boolean isUsedFallback() { return usedFallback; }
    public void setUsedFallback(boolean usedFallback) { this.usedFallback = usedFallback; }

    public Double getUsedAvgWindKnots() { return usedAvgWindKnots; }
    public void setUsedAvgWindKnots(Double usedAvgWindKnots) { this.usedAvgWindKnots = usedAvgWindKnots; }

    public Double getUsedMaxWaveM() { return usedMaxWaveM; }
    public void setUsedMaxWaveM(Double usedMaxWaveM) { this.usedMaxWaveM = usedMaxWaveM; }

    public Boolean getRouteViable() { return routeViable; }
    public void setRouteViable(Boolean routeViable) { this.routeViable = routeViable; }

    public String getNonViableReason() { return nonViableReason; }
    public void setNonViableReason(String nonViableReason) { this.nonViableReason = nonViableReason; }

    public Double getOverallHazardProbability() { return overallHazardProbability; }
    public void setOverallHazardProbability(Double overallHazardProbability) { this.overallHazardProbability = overallHazardProbability; }

    public List<WeatherHazardProbability> getHazards() { return hazards; }
    public void setHazards(List<WeatherHazardProbability> hazards) { this.hazards = hazards; }
}
