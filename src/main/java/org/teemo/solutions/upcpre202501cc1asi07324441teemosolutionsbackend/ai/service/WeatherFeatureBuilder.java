package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.WeatherDelayRequest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

@Component
public class WeatherFeatureBuilder {

    private static final Logger log = LoggerFactory.getLogger(WeatherFeatureBuilder.class);
    private static final String[] SEASONS = {"DJF","DJF","MAM","MAM","MAM","JJA","JJA","JJA","SON","SON","SON","DJF"};

    private final WeatherProvider provider;

    public WeatherFeatureBuilder(WeatherProvider provider) {
        this.provider = provider;
    }

    public static record Features(
            float distanceKm,
            float plannedHours,
            float avgWindKnots,
            float maxWaveM,
            Double rainMm,
            Double visibilityKm,
            Double originQueue,
            Double destQueue,
            Double portCongestionIdx,
            Double originLat,
            Double originLon,
            Double destLat,
            Double destLon,
            String portOrigin,
            String portDest,
            Integer month,
            String season
    ) {}

    public Features build(WeatherDelayRequest req) {
        if (req.getDistanceKm() == null || req.getCruiseSpeedKnots() == null)
            throw new IllegalArgumentException("distanceKm y cruiseSpeedKnots son requeridos");

        float distance = req.getDistanceKm().floatValue();
        float plannedHours = (float)(distance / (req.getCruiseSpeedKnots().floatValue() * 1.852f));

        Float avgWind;
        Float maxWave;

        // Modo manual
        if (req.getAvgWindKnots() != null && req.getMaxWaveM() != null) {
            avgWind = req.getAvgWindKnots().floatValue();
            maxWave = req.getMaxWaveM().floatValue();
        } else if (req.getOriginLat() != null && req.getOriginLon() != null &&
                req.getDestLat() != null && req.getDestLon() != null &&
                req.getDepartureTimeIso() != null && !req.getDepartureTimeIso().isBlank()) {

            Instant dep = Instant.parse(req.getDepartureTimeIso());
            Instant end = dep.plusSeconds((long)Math.round(plannedHours * 3600));

            // muestreamos 6 waypoints + origen (7 puntos)
            var sum = provider.fetchSummaryAlongRoute(
                    req.getOriginLat(), req.getOriginLon(),
                    req.getDestLat(), req.getDestLon(),
                    dep, end, 6);

            avgWind = (float) sum.avgWindKnots();
            maxWave = (float) sum.maxWaveM();
        } else {
            throw new IllegalArgumentException("Sin datos climáticos: envía (avgWindKnots,maxWaveM) o coords+departureTimeIso.");
        }

        var depInfo = resolveDepartureInfo(req.getDepartureTimeIso());
        return new Features(distance, plannedHours,
                avgWind,
                maxWave,
                req.getRainMm(),
                req.getVisibilityKm(),
                req.getOriginQueue(),
                req.getDestQueue(),
                resolveCongestion(req.getPortCongestionIdx(), req.getOriginQueue(), req.getDestQueue()),
                req.getOriginLat(),
                req.getOriginLon(),
                req.getDestLat(),
                req.getDestLon(),
                req.getPortOrigin(),
                req.getPortDest(),
                depInfo.month(),
                depInfo.season());
    }

    private static Double resolveCongestion(Double provided, Double originQueue, Double destQueue) {
        if (provided != null) return provided;
        if (originQueue != null && destQueue != null) {
            return (originQueue + destQueue) / 2.0;
        }
        return null;
    }

    private record DepartureInfo(Integer month, String season) {}

    private DepartureInfo resolveDepartureInfo(String departureIso) {
        Instant reference = Instant.now();
        if (departureIso != null && !departureIso.isBlank()) {
            try {
                reference = Instant.parse(departureIso);
            } catch (DateTimeParseException ex) {
                log.warn("departureTimeIso no es ISO-8601 válido: '{}'", departureIso);
            }
        }
        int month = reference.atZone(ZoneOffset.UTC).getMonthValue();
        return new DepartureInfo(month, seasonFromMonth(month));
    }

    private static String seasonFromMonth(int month) {
        if (month < 1 || month > 12) return "DJF";
        return SEASONS[month - 1];
    }
}
