package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Probabilidad heurística de un evento/clima sobre la ruta.
 * Esta clase es usada internamente por el servicio de detección y luego
 * mapeada a HazardAssessmentResponse.Hazard para la respuesta pública.
 */
@Schema(name = "WeatherHazardProbability", description = "Resultado heurístico por tipo de peligro en un tramo/zona.")
public class WeatherHazardProbability {

    @Schema(
            description = "Tipo lógico del peligro detectado.",
            example = "HURRICANE",
            allowableValues = {"HURRICANE", "ICE", "MAREAJE"}
    )
    private String type;

    @Schema(
            description = "Probabilidad estimada del evento en la zona/tramo [0..1].",
            example = "0.45",
            minimum = "0.0",
            maximum = "1.0"
    )
    private double probability;

    @Schema(
            description = "Nombre de la zona o región asociada al evento.",
            example = "Franja tropical propensa a ciclones",
            nullable = true
    )
    private String zoneName;

    // —— Coordenadas para dibujar en el mapa ——
    @Schema(
            description = "Latitud del centro aproximado de la zona de riesgo (decimal degrees).",
            example = "13.3",
            nullable = true
    )
    private Double latCenter;

    @Schema(
            description = "Longitud del centro aproximado de la zona de riesgo (decimal degrees).",
            example = "-47.1",
            nullable = true
    )
    private Double lonCenter;

    @Schema(
            description = "Radio aproximado de influencia de la zona de riesgo, en kilómetros.",
            example = "600.0",
            nullable = true
    )
    private Double radiusKm;

    @Schema(
            description = "Mes de referencia de la evaluación (1..12). Útil para estacionalidad.",
            example = "8",
            nullable = true,
            minimum = "1",
            maximum = "12"
    )
    private Integer month;

    public WeatherHazardProbability() {}

    // Getters / Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getProbability() { return probability; }
    public void setProbability(double probability) { this.probability = probability; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public Double getLatCenter() { return latCenter; }
    public void setLatCenter(Double latCenter) { this.latCenter = latCenter; }

    public Double getLonCenter() { return lonCenter; }
    public void setLonCenter(Double lonCenter) { this.lonCenter = lonCenter; }

    public Double getRadiusKm() { return radiusKm; }
    public void setRadiusKm(Double radiusKm) { this.radiusKm = radiusKm; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
}
