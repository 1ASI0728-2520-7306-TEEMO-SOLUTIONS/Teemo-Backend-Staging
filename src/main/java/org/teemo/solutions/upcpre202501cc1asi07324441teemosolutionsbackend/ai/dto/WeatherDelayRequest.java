package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class WeatherDelayRequest {
    // EXISTENTES
    private Double distanceKm;
    private Double cruiseSpeedKnots;
    private Double avgWindKnots;   // opcional si vamos a calcular
    private Double maxWaveM;       // opcional si vamos a calcular
    private String departureTimeIso;
    @Schema(example = "3.2", description = "Precipitación acumulada en mm durante el tramo", nullable = true)
    private Double rainMm;
    @Schema(example = "15.5", description = "Visibilidad estimada en km", nullable = true)
    private Double visibilityKm;
    @Schema(example = "4.0", description = "Índice de cola/espera en puerto origen (0-10 aprox.)", nullable = true)
    private Double originQueue;
    @Schema(example = "3.0", description = "Índice de cola/espera en puerto destino (0-10 aprox.)", nullable = true)
    private Double destQueue;
    @Schema(example = "0.85", description = "Índice agregado de congestión portuaria", nullable = true)
    private Double portCongestionIdx;

    // NUEVOS (opcionales) – si están presentes, usamos Open-Meteo
    @Schema(example = "-12.0464", description = "Latitud origen (ej. Callao/Lima)")
    private Double originLat;
    @Schema(example = "-77.0428", description = "Longitud origen")
    private Double originLon;

    @Schema(example = "35.6762", description = "Latitud destino (ej. Yokohama/Tokyo Bay)")
    private Double destLat;
    @Schema(example = "139.6503", description = "Longitud destino")
    private Double destLon;
    @Schema(example = "Callao", description = "Nombre del puerto de origen para mapearlo con el encoder", nullable = true)
    private String portOrigin;
    @Schema(example = "Yokohama", description = "Nombre del puerto destino para mapearlo con el encoder", nullable = true)
    private String portDest;

    // getters/setters...
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getCruiseSpeedKnots() { return cruiseSpeedKnots; }
    public void setCruiseSpeedKnots(Double cruiseSpeedKnots) { this.cruiseSpeedKnots = cruiseSpeedKnots; }
    public Double getAvgWindKnots() { return avgWindKnots; }
    public void setAvgWindKnots(Double avgWindKnots) { this.avgWindKnots = avgWindKnots; }
    public Double getMaxWaveM() { return maxWaveM; }
    public void setMaxWaveM(Double maxWaveM) { this.maxWaveM = maxWaveM; }
    public String getDepartureTimeIso() { return departureTimeIso; }
    public void setDepartureTimeIso(String departureTimeIso) { this.departureTimeIso = departureTimeIso; }
    public Double getRainMm() { return rainMm; }
    public void setRainMm(Double rainMm) { this.rainMm = rainMm; }
    public Double getVisibilityKm() { return visibilityKm; }
    public void setVisibilityKm(Double visibilityKm) { this.visibilityKm = visibilityKm; }
    public Double getOriginQueue() { return originQueue; }
    public void setOriginQueue(Double originQueue) { this.originQueue = originQueue; }
    public Double getDestQueue() { return destQueue; }
    public void setDestQueue(Double destQueue) { this.destQueue = destQueue; }
    public Double getPortCongestionIdx() { return portCongestionIdx; }
    public void setPortCongestionIdx(Double portCongestionIdx) { this.portCongestionIdx = portCongestionIdx; }
    public Double getOriginLat() { return originLat; }
    public void setOriginLat(Double originLat) { this.originLat = originLat; }
    public Double getOriginLon() { return originLon; }
    public void setOriginLon(Double originLon) { this.originLon = originLon; }
    public Double getDestLat() { return destLat; }
    public void setDestLat(Double destLat) { this.destLat = destLat; }
    public Double getDestLon() { return destLon; }
    public void setDestLon(Double destLon) { this.destLon = destLon; }
    public String getPortOrigin() { return portOrigin; }
    public void setPortOrigin(String portOrigin) { this.portOrigin = portOrigin; }
    public String getPortDest() { return portDest; }
    public void setPortDest(String portDest) { this.portDest = portDest; }
}
