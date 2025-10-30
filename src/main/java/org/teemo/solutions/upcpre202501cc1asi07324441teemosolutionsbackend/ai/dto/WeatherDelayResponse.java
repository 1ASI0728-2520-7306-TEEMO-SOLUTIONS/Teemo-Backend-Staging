package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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

    // 🔹 NUEVOS CAMPOS (opcionales) para diagnóstico
    @Schema(example = "21.4", description = "Viento promedio usado por el modelo (knots)", nullable = true)
    private Double usedAvgWindKnots;

    @Schema(example = "3.6", description = "Altura máxima de ola usada por el modelo (m)", nullable = true)
    private Double usedMaxWaveM;

    // getters/setters
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
}
