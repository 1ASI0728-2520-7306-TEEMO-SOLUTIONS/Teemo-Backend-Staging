package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class WeatherDelayRequest {
    // EXISTENTES
    private Double distanceKm;
    private Double cruiseSpeedKnots;
    private Double avgWindKnots;   // opcional si vamos a calcular
    private Double maxWaveM;       // opcional si vamos a calcular
    private String departureTimeIso;

    // NUEVOS (opcionales) – si están presentes, usamos Open-Meteo
    @Schema(example = "-12.0464", description = "Latitud origen (ej. Callao/Lima)")
    private Double originLat;
    @Schema(example = "-77.0428", description = "Longitud origen")
    private Double originLon;

    @Schema(example = "35.6762", description = "Latitud destino (ej. Yokohama/Tokyo Bay)")
    private Double destLat;
    @Schema(example = "139.6503", description = "Longitud destino")
    private Double destLon;

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
    public Double getOriginLat() { return originLat; }
    public void setOriginLat(Double originLat) { this.originLat = originLat; }
    public Double getOriginLon() { return originLon; }
    public void setOriginLon(Double originLon) { this.originLon = originLon; }
    public Double getDestLat() { return destLat; }
    public void setDestLat(Double destLat) { this.destLat = destLat; }
    public Double getDestLon() { return destLon; }
    public void setDestLon(Double destLon) { this.destLon = destLon; }
}
