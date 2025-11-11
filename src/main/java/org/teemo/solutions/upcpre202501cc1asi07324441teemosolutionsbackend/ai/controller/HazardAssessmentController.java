package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.service.WeatherHazardDetectionService;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.service.WeatherHazardDetectionService.HazardEval;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador REST que evalúa eventos naturales o climáticos a lo largo de la ruta marítima.
 * Integra con WeatherHazardDetectionService (heurístico, sin APIs externas aún).
 */
@RestController
@RequestMapping("/api/ai")
public class HazardAssessmentController {

    private final WeatherHazardDetectionService detectionService;

    public HazardAssessmentController(WeatherHazardDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    // =============== ENDPOINT PRINCIPAL ======================
    @Operation(
            summary = "Evalúa probabilidad de eventos de riesgo (huracanes, hielo, mareaje) en la ruta marítima.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Evaluación exitosa",
                            content = @Content(schema = @Schema(implementation = HazardAssessmentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Parámetros inválidos o incompletos")
            }
    )
    @PostMapping(value = "/hazard-assessment", consumes = "application/json", produces = "application/json")
    public ResponseEntity<HazardAssessmentResponse> assess(@RequestBody HazardAssessmentRequest req) {

        // ===== Validación mínima =====
        if (req.getOriginLat() == null || req.getOriginLon() == null ||
                req.getDestLat() == null   || req.getDestLon() == null) {
            return ResponseEntity.badRequest().build();
        }

        // ===== Parse de fecha =====
        Instant departure = Instant.now();
        if (req.getDepartureTimeIso() != null && !req.getDepartureTimeIso().isBlank()) {
            try {
                departure = Instant.parse(req.getDepartureTimeIso());
            } catch (DateTimeParseException ex) {
                // Usamos ahora() si la fecha no es válida
            }
        }

        // ===== Evaluación heurística =====
        HazardEval eval = detectionService.evaluate(
                req.getOriginLat(), req.getOriginLon(),
                req.getDestLat(), req.getDestLon(),
                departure
        );

        // ===== Construcción de respuesta =====
        HazardAssessmentResponse response = buildResponse(req, eval, departure);

        return ResponseEntity.ok(response);
    }

    // ===========================================================
    // Construcción de respuesta completa
    // ===========================================================
    private HazardAssessmentResponse buildResponse(
            HazardAssessmentRequest req,
            HazardEval eval,
            Instant departure
    ) {
        HazardAssessmentResponse out = new HazardAssessmentResponse();

        int month = departure.atZone(ZoneOffset.UTC).getMonthValue();
        out.setMonth(month);
        out.setSeason(seasonOf(month));
        out.setHemisphere(hemisphereOf((req.getOriginLat() + req.getDestLat()) / 2.0));

        double routeKm = haversineKm(req.getOriginLat(), req.getOriginLon(), req.getDestLat(), req.getDestLon());
        out.setRouteDistanceKm(routeKm);

        if (req.getCruiseSpeedKnots() != null && req.getCruiseSpeedKnots() > 0) {
            double speedKmH = req.getCruiseSpeedKnots() * 1.852;
            out.setPlannedHours(routeKm / speedKmH);
        } else {
            out.setPlannedHours(0);
        }

        // ===== Segmento principal =====
        HazardAssessmentResponse.Segment seg = new HazardAssessmentResponse.Segment();
        seg.setStartLat(req.getOriginLat());
        seg.setStartLon(req.getOriginLon());
        seg.setEndLat(req.getDestLat());
        seg.setEndLon(req.getDestLon());
        seg.setHazards(new ArrayList<>());

        // Mapeo de cada evento detectado
        for (WeatherHazardProbability whp : eval.hazards) {
            HazardAssessmentResponse.Hazard hz = new HazardAssessmentResponse.Hazard();
            hz.setType(mapType(whp.getType()));  // ICE → ICE, MAREAJE → LARGE_SWELL, etc.
            hz.setProbability(clamp01(whp.getProbability()));
            hz.setSeverity(severityOf(whp.getProbability()));
            hz.setWindowStartIso(departure.toString());
            hz.setWindowEndIso(departure.plusSeconds(7 * 24 * 3600).toString()); // 7 días
            hz.setRegionName(whp.getZoneName());
            hz.setSource("heuristic-internal");
            hz.setRationale(rationaleFor(hz.getType(), whp.getProbability(), out.getSeason()));

            // NUEVO: coordenadas para dibujar en mapa
            hz.setLatCenter(whp.getLatCenter());
            hz.setLonCenter(whp.getLonCenter());
            hz.setRadiusKm(whp.getRadiusKm());

            seg.getHazards().add(hz);
        }

        // Bandera de inviabilidad
        seg.setNotViableDueToIce(!eval.routeViable);
        seg.setAdvisory(eval.nonViableReason);

        out.setSegments(List.of(seg));
        return out;
    }

    // ===========================================================
    // Utilidades de mapeo y formato
    // ===========================================================

    private static String mapType(String internal) {
        if (internal == null) return "UNKNOWN";
        return switch (internal) {
            case "ICE" -> "ICE";
            case "HURRICANE" -> "HURRICANE";
            case "MAREAJE" -> "LARGE_SWELL";
            default -> internal.toUpperCase();
        };
    }

    private static String severityOf(double p) {
        if (p >= 0.6) return "HIGH";
        if (p >= 0.3) return "MEDIUM";
        return "LOW";
    }

    private static String seasonOf(int month) {
        // DJF(12,1,2) MAM(3,4,5) JJA(6,7,8) SON(9,10,11)
        if (month == 12 || month <= 2) return "DJF";
        if (month <= 5) return "MAM";
        if (month <= 8) return "JJA";
        return "SON";
    }

    private static String hemisphereOf(double lat) {
        if (lat > 1e-6) return "N";
        if (lat < -1e-6) return "S";
        return "EQUATOR";
    }

    private static String rationaleFor(String type, double p, String season) {
        return switch (type) {
            case "HURRICANE" -> "Probabilidad basada en franja tropical y temporada " + season;
            case "ICE" -> "Probabilidad basada en alta latitud y temporada fría " + season;
            case "LARGE_SWELL" -> "Oleaje persistente por swell del Pacífico Sur";
            default -> "Heurística según zona/estación";
        };
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0088;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static double clamp01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }
}
