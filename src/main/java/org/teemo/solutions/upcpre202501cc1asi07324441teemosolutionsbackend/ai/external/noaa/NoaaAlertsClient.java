package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.external.noaa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.configuration.NoaaAlertsProperties;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.WeatherHazardProbability;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Cliente mínimo para consultar alertas activas de ciclones del API público de NOAA (weather.gov).
 */
@Component
public class NoaaAlertsClient {

    private static final Logger log = LoggerFactory.getLogger(NoaaAlertsClient.class);

    private final WebClient webClient;
    private final NoaaAlertsProperties properties;

    public NoaaAlertsClient(WebClient.Builder builder, NoaaAlertsProperties properties) {
        this.properties = properties;
        this.webClient = builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, properties.getUserAgent())
                .build();
    }

    public List<WeatherHazardProbability> fetchActiveCycloneAlerts(
            double latA, double lonA,
            double latB, double lonB,
            Instant departure
    ) {
        if (!properties.isEnabled()) {
            return List.of();
        }

        URI uri = buildUri(latA, lonA, latB, lonB);
        try {
            NoaaAlertResponse response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(NoaaAlertResponse.class)
                    .block(properties.getTimeout());

            if (response == null || CollectionUtils.isEmpty(response.features)) {
                return List.of();
            }

            double fallbackLat = (latA + latB) / 2.0;
            double fallbackLon = (lonA + lonB) / 2.0;
            int month = departure.atZone(ZoneOffset.UTC).getMonthValue();

            List<WeatherHazardProbability> hazards = new ArrayList<>();
            for (NoaaAlertResponse.Feature feature : response.features) {
                WeatherHazardProbability hz = mapFeature(feature, month, fallbackLat, fallbackLon);
                if (hz != null) {
                    hazards.add(hz);
                }
            }
            return hazards;
        } catch (Exception ex) {
            log.warn("Fallo consultando alertas NOAA en {}: {}", uri, ex.getMessage());
            return List.of();
        }
    }

    private URI buildUri(double latA, double lonA, double latB, double lonB) {
        double minLat = clampLat(Math.min(latA, latB) - properties.getLatPaddingDeg());
        double maxLat = clampLat(Math.max(latA, latB) + properties.getLatPaddingDeg());
        double minLon = clampLon(Math.min(lonA, lonB) - properties.getLonPaddingDeg());
        double maxLon = clampLon(Math.max(lonA, lonB) + properties.getLonPaddingDeg());

        String bbox = String.format(Locale.US, "%.2f,%.2f,%.2f,%.2f", minLon, minLat, maxLon, maxLat);

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/alerts/active")
                .queryParam("status", "actual")
                .queryParam("message_type", "alert")
                .queryParam("limit", properties.getMaxResults())
                .queryParam("urgency", "Immediate")
                .queryParam("urgency", "Expected")
                .queryParam("bbox", bbox);

        for (String event : properties.getCycloneEvents()) {
            builder.queryParam("event", event);
        }

        return builder.build(true).toUri();
    }

    private WeatherHazardProbability mapFeature(NoaaAlertResponse.Feature feature, int month, double fallbackLat, double fallbackLon) {
        if (feature == null || feature.properties == null) {
            return null;
        }

        double probability = probabilityFromAlert(feature.properties);
        if (probability <= 0.0) {
            return null;
        }

        WeatherHazardProbability hz = new WeatherHazardProbability();
        hz.setType("HURRICANE");
        hz.setProbability(probability);
        hz.setZoneName(buildZoneName(feature.properties));
        hz.setMonth(month);

        Coordinate center = resolveCenter(feature.geometry, fallbackLat, fallbackLon);
        hz.setLatCenter(center.lat());
        hz.setLonCenter(center.lon());
        hz.setRadiusKm(properties.getDefaultRadiusKm());

        return hz;
    }

    private String buildZoneName(NoaaAlertResponse.Properties properties) {
        String area = properties.areaDesc != null ? properties.areaDesc : "zona tropical";
        String event = properties.event != null ? properties.event : "Cyclone Alert";
        return String.format("%s - %s", event, area);
    }

    private double probabilityFromAlert(NoaaAlertResponse.Properties properties) {
        double base = switch (normalize(properties.severity)) {
            case "EXTREME" -> 0.9;
            case "SEVERE" -> 0.75;
            case "MODERATE" -> 0.55;
            case "MINOR" -> 0.4;
            default -> 0.35;
        };

        String certainty = normalize(properties.certainty);
        if ("OBSERVED".equals(certainty)) {
            base += 0.08;
        } else if ("LIKELY".equals(certainty)) {
            base += 0.05;
        } else if ("POSSIBLE".equals(certainty)) {
            base -= 0.05;
        } else if ("UNLIKELY".equals(certainty)) {
            base -= 0.1;
        }

        if (properties.event != null && properties.event.toLowerCase(Locale.ROOT).contains("watch")) {
            base -= 0.1;
        }

        return clamp01(base);
    }

    private Coordinate resolveCenter(NoaaAlertResponse.Geometry geometry, double fallbackLat, double fallbackLon) {
        List<List<Double>> ring = extractFirstRing(geometry);
        if (ring == null || ring.isEmpty()) {
            return new Coordinate(fallbackLat, fallbackLon);
        }

        double sumLat = 0.0;
        double sumLon = 0.0;
        int count = 0;
        for (List<Double> pair : ring) {
            if (pair.size() < 2) continue;
            sumLon += pair.get(0);
            sumLat += pair.get(1);
            count++;
        }
        if (count == 0) {
            return new Coordinate(fallbackLat, fallbackLon);
        }
        return new Coordinate(sumLat / count, sumLon / count);
    }

    @SuppressWarnings("unchecked")
    private List<List<Double>> extractFirstRing(NoaaAlertResponse.Geometry geometry) {
        if (geometry == null || CollectionUtils.isEmpty(geometry.coordinates)) {
            return null;
        }
        Object firstElement = geometry.coordinates.get(0);
        if ("Polygon".equalsIgnoreCase(geometry.type)) {
            return convertRing(firstElement);
        }
        if ("MultiPolygon".equalsIgnoreCase(geometry.type)) {
            if (!(firstElement instanceof List<?> polygonList) || polygonList.isEmpty()) {
                return null;
            }
            Object firstRing = polygonList.get(0);
            return convertRing(firstRing);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<List<Double>> convertRing(Object ringObject) {
        if (!(ringObject instanceof List<?> ringList) || ringList.isEmpty()) {
            return null;
        }
        List<List<Double>> points = new ArrayList<>();
        for (Object point : ringList) {
            if (!(point instanceof List<?> coords) || coords.size() < 2) {
                continue;
            }
            Object lonObj = coords.get(0);
            Object latObj = coords.get(1);
            if (lonObj instanceof Number lon && latObj instanceof Number lat) {
                points.add(List.of(lon.doubleValue(), lat.doubleValue()));
            }
        }
        return points;
    }

    private double clampLat(double value) {
        return Math.max(-90.0, Math.min(90.0, value));
    }

    private double clampLon(double value) {
        return Math.max(-180.0, Math.min(180.0, value));
    }

    private String normalize(String input) {
        return input == null ? "" : input.trim().toUpperCase(Locale.ROOT);
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private record Coordinate(double lat, double lon) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class NoaaAlertResponse {
        @JsonProperty("features")
        private List<Feature> features;

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static final class Feature {
            @JsonProperty("properties")
            private Properties properties;
            @JsonProperty("geometry")
            private Geometry geometry;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static final class Properties {
            @JsonProperty("event")
            private String event;
            @JsonProperty("severity")
            private String severity;
            @JsonProperty("certainty")
            private String certainty;
            @JsonProperty("areaDesc")
            private String areaDesc;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static final class Geometry {
            @JsonProperty("type")
            private String type;
            @JsonProperty("coordinates")
            private List<Object> coordinates;
        }
    }
}
