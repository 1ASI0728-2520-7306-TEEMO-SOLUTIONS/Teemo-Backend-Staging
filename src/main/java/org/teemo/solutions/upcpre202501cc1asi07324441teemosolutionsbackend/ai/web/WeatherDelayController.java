package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.WeatherDelayRequest;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.WeatherDelayResponse;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.service.WeatherDelayService;

@RestController
@RequestMapping("/api/ai")
public class WeatherDelayController {

    private final WeatherDelayService service;

    public WeatherDelayController(WeatherDelayService service) {
        this.service = service;
    }

    @PostMapping("/predict-weather-delay")
    public ResponseEntity<?> predict(@RequestBody WeatherDelayRequest req) {
        // Requisitos mínimos siempre
        if (req.getDistanceKm() == null || req.getCruiseSpeedKnots() == null) {
            return ResponseEntity.badRequest().body("distanceKm y cruiseSpeedKnots son requeridos.");
        }

        // Modo A: manual (clima ya viene)
        boolean hasManual = req.getAvgWindKnots() != null && req.getMaxWaveM() != null;

        // Modo B: Open-Meteo (coords + salida)
        boolean hasCoords = req.getOriginLat() != null && req.getOriginLon() != null
                && req.getDestLat() != null && req.getDestLon() != null
                && req.getDepartureTimeIso() != null && !req.getDepartureTimeIso().isBlank();

        if (!hasManual && !hasCoords) {
            return ResponseEntity.badRequest().body(
                    "Debes enviar (avgWindKnots y maxWaveM) O bien (originLat/originLon/destLat/destLon y departureTimeIso) para usar Open-Meteo."
            );
        }

        WeatherDelayResponse resp = service.predict(req);
        return ResponseEntity.ok(resp);
    }
}
