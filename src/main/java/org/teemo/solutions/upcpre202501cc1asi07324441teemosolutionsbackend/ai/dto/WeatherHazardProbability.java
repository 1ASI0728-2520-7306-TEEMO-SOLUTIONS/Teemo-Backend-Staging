package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WeatherHazardProbability {
    // Tipo de evento: ICE, HURRICANE, MAREAJE, OIL_SPILL, MPA, OTHER
    private String type;
    // Probabilidad [0..1] de encontrar ese evento en el corredor de ruta
    private double probability;
    // Texto humano de la zona (ej. “Mar de Labrador”, “Caribe Occidental”)
    private String zoneName;
    // Centro aproximado del área de riesgo (opcional)
    private Double latCenter;
    private Double lonCenter;
    // Radio aproximado (km) o tamaño de celda (opcional)
    private Double radiusKm;
    // Mes o ventana temporal de validez (1..12) (opcional)
    private Integer month;
}
