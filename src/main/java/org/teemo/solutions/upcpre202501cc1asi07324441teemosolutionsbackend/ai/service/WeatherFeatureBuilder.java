package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.service;

import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.WeatherDelayRequest;

import java.time.Instant;

@Component
public class WeatherFeatureBuilder {

    private final WeatherProvider provider;

    public WeatherFeatureBuilder(WeatherProvider provider) {
        this.provider = provider;
    }

    public static record Features(float distanceKm, float plannedHours, float avgWindKnots, float maxWaveM) {}

    public Features build(WeatherDelayRequest req) {
        if (req.getDistanceKm() == null || req.getCruiseSpeedKnots() == null)
            throw new IllegalArgumentException("distanceKm y cruiseSpeedKnots son requeridos");

        float distance = req.getDistanceKm().floatValue();
        float plannedHours = (float)(distance / (req.getCruiseSpeedKnots().floatValue() * 1.852f));

        // Modo manual
        if (req.getAvgWindKnots() != null && req.getMaxWaveM() != null) {
            return new Features(distance, plannedHours,
                    req.getAvgWindKnots().floatValue(),
                    req.getMaxWaveM().floatValue());
        }

        // Modo Open-Meteo con ruta completa
        if (req.getOriginLat() != null && req.getOriginLon() != null &&
                req.getDestLat() != null && req.getDestLon() != null &&
                req.getDepartureTimeIso() != null && !req.getDepartureTimeIso().isBlank()) {

            Instant dep = Instant.parse(req.getDepartureTimeIso());
            Instant end = dep.plusSeconds((long)Math.round(plannedHours * 3600));

            // muestreamos 6 waypoints + origen (7 puntos)
            var sum = provider.fetchSummaryAlongRoute(
                    req.getOriginLat(), req.getOriginLon(),
                    req.getDestLat(), req.getDestLon(),
                    dep, end, 6);

            return new Features(distance, plannedHours,
                    (float) sum.avgWindKnots(), (float) sum.maxWaveM());
        }

        throw new IllegalArgumentException("Sin datos climáticos: envía (avgWindKnots,maxWaveM) o coords+departureTimeIso.");
    }
}
