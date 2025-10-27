package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WeatherProvider {
    private static final Logger log = LoggerFactory.getLogger(WeatherProvider.class);
    private final WebClient http;

    public WeatherProvider(WebClient http) {
        this.http = http;
    }

    private static final double MS_TO_KNOTS = 1.9438444924406;
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);

    public record WeatherSummary(double avgWindKnots, double maxWaveM) {}

    /** ========= GRAN CÍRCULO (waypoints) ========= */

    /** Devuelve N waypoints (excluye el origen, incluye el destino) a lo largo del gran-círculo. */
    public List<double[]> greatCircleWaypoints(double lat1Deg, double lon1Deg,
                                               double lat2Deg, double lon2Deg, int points) {
        // conversión a radianes
        double lat1 = Math.toRadians(lat1Deg), lon1 = Math.toRadians(lon1Deg);
        double lat2 = Math.toRadians(lat2Deg), lon2 = Math.toRadians(lon2Deg);

        // convertir a vectores 3D en la esfera unidad
        double[] p1 = sphToCart(lat1, lon1);
        double[] p2 = sphToCart(lat2, lon2);

        // ángulo entre p1 y p2
        double omega = Math.acos(clamp(dot(p1, p2), -1.0, 1.0));

        List<double[]> out = new ArrayList<>();
        for (int i = 1; i <= points; i++) {
            double f = (double) i / (double) points; // 0..1
            double sinOmega = Math.sin(omega);
            double a = Math.sin((1 - f) * omega) / sinOmega;
            double b = Math.sin(f * omega) / sinOmega;
            double[] p = new double[] {
                    a * p1[0] + b * p2[0],
                    a * p1[1] + b * p2[1],
                    a * p1[2] + b * p2[2]
            };
            // normaliza y convierte a lat/lon
            double norm = Math.sqrt(p[0]*p[0] + p[1]*p[1] + p[2]*p[2]);
            p[0] /= norm; p[1] /= norm; p[2] /= norm;
            double lat = Math.asin(p[2]);
            double lon = Math.atan2(p[1], p[0]);
            out.add(new double[] { Math.toDegrees(lat), normalizeLonDeg(Math.toDegrees(lon)) });
        }
        return out;
    }

    private static double[] sphToCart(double lat, double lon) {
        double clat = Math.cos(lat);
        return new double[] { clat * Math.cos(lon), clat * Math.sin(lon), Math.sin(lat) };
    }
    private static double dot(double[] a, double[] b) { return a[0]*b[0]+a[1]*b[1]+a[2]*b[2]; }
    private static double clamp(double v, double lo, double hi){ return Math.max(lo, Math.min(hi, v)); }
    private static double normalizeLonDeg(double lon){
        // normaliza a [-180,180)
        lon = ((lon + 180) % 360 + 360) % 360 - 180;
        return lon;
    }

    /** ========= LLAMADAS A OPEN-METEO ========= */

    /** Obtiene promedio horario de viento (knots) y ola máxima (m) para un punto [lat,lon] y rango [start,end]. */
    private WeatherSummary fetchPoint(double lat, double lon, Instant start, Instant end) {
        try {
            String startDate = ISO_DATE.format(start);
            String endDate   = ISO_DATE.format(end);

            // viento 10m (m/s) -> knots
            Map windJson = http.get()
                    .uri(uri -> uri.scheme("https").host("api.open-meteo.com").path("/v1/forecast")
                            .queryParam("latitude", lat)
                            .queryParam("longitude", lon)
                            .queryParam("hourly", "wind_speed_10m")
                            .queryParam("start_date", startDate)
                            .queryParam("end_date", endDate)
                            .queryParam("timezone", "UTC")
                            .build())
                    .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            double avgWindKnots = 0; int windCount = 0;
            if (windJson != null && windJson.get("hourly") instanceof Map hourly) {
                var times = (java.util.List<?>) ((Map<?, ?>) hourly).get("time");
                var vals  = (java.util.List<?>) ((Map<?, ?>) hourly).get("wind_speed_10m");
                if (times != null && vals != null && times.size() == vals.size()) {
                    double sum = 0;
                    for (int i = 0; i < times.size(); i++) {
                        Object t = times.get(i), v = vals.get(i);
                        if (t instanceof String ts && v instanceof Number n) {
                            Instant it = Instant.parse(ts + ":00Z".replace("Z:00Z","Z")); // por si viene sin segundos
                            if (!it.isBefore(start) && !it.isAfter(end)) {
                                sum += n.doubleValue();
                                windCount++;
                            }
                        }
                    }
                    if (windCount > 0) avgWindKnots = (sum / windCount) * MS_TO_KNOTS;
                }
            }

            // --- OLA ---
            Map waveJson = http.get()
                    .uri(uri -> uri.scheme("https").host("marine-api.open-meteo.com").path("/v1/marine")
                            .queryParam("latitude", lat)
                            .queryParam("longitude", lon)
                            .queryParam("hourly", "wave_height")
                            .queryParam("start_date", startDate)
                            .queryParam("end_date", endDate)
                            .queryParam("timezone", "UTC")
                            .build())
                    .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            double maxWaveM = 0;
            if (waveJson != null && waveJson.get("hourly") instanceof Map hourly) {
                var times = (java.util.List<?>) ((Map<?, ?>) hourly).get("time");
                var vals  = (java.util.List<?>) ((Map<?, ?>) hourly).get("wave_height");
                if (times != null && vals != null && times.size() == vals.size()) {
                    for (int i = 0; i < times.size(); i++) {
                        Object t = times.get(i), v = vals.get(i);
                        if (t instanceof String ts && v instanceof Number n) {
                            Instant it = Instant.parse(ts + ":00Z".replace("Z:00Z","Z"));
                            if (!it.isBefore(start) && !it.isAfter(end)) {
                                maxWaveM = Math.max(maxWaveM, n.doubleValue());
                            }
                        }
                    }
                }
            }

            log.debug("OM point(lat={},lon={}) windObs={}, avgKnots={}, maxWaveM={}",
                    String.format("%.4f", lat), String.format("%.4f", lon),
                    windCount, String.format("%.2f", avgWindKnots), String.format("%.2f", maxWaveM));
            return new WeatherSummary(avgWindKnots, maxWaveM);

        } catch (Exception e) {
            log.warn("Open-Meteo fallo en punto lat={}, lon={} -> {}", lat, lon, e.getMessage());
            return new WeatherSummary(0, 0); // sin inventar valores
        }
    }

    /** Consulta N waypoints a lo largo del gran-círculo y agrega resultados. */
    public WeatherSummary fetchSummaryAlongRoute(double oLat, double oLon, double dLat, double dLon,
                                                 Instant start, Instant end, int waypoints) {
        List<double[]> pts = greatCircleWaypoints(oLat, oLon, dLat, dLon, waypoints);
        // incluimos además el origen (opcional)
        pts.add(0, new double[]{oLat, oLon});

        double windSum = 0; int windObs = 0;
        double maxWave = 0;

        for (double[] p : pts) {
            var s = fetchPoint(p[0], p[1], start, end);
            if (s.avgWindKnots > 0) { windSum += s.avgWindKnots; windObs++; }
            if (s.maxWaveM > 0) maxWave = Math.max(maxWave, s.maxWaveM);
        }
        double avgWind = windObs > 0 ? (windSum / windObs) : 0;

        log.info("OM route agg points={}, avgWindKnots={}, maxWaveM={}",
                pts.size(), String.format("%.2f", avgWind), String.format("%.2f", maxWave));

        return new WeatherSummary(avgWind, maxWave);
    }
}
