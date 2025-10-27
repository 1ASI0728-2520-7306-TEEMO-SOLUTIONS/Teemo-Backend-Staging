package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.service;

import ai.onnxruntime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.WeatherDelayRequest;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.WeatherDelayResponse;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@Service
public class WeatherDelayService {
    private static final Logger log = LoggerFactory.getLogger(WeatherDelayService.class);

    private final OrtEnvironment env;
    private final OrtSession session;
    private final String inputName;
    private final String outputName;

    private final WeatherFeatureBuilder featureBuilder;

    public WeatherDelayService(OrtEnvironment env, OrtSession session, WeatherFeatureBuilder featureBuilder) throws OrtException {
        this.env = env;
        this.session = session;
        this.featureBuilder = featureBuilder;
        this.inputName = session.getInputInfo().keySet().iterator().next();    // "input"
        this.outputName = session.getOutputInfo().keySet().iterator().next();  // "variable"
        log.info("[ONNX] inputName='{}', outputName='{}'", inputName, outputName);
    }

    public WeatherDelayResponse predict(WeatherDelayRequest req) {
        long t0 = System.currentTimeMillis();
        try {
            //  Validación mínima (dejamos que FeatureBuilder resuelva clima manual/OM)
            if (req.getDistanceKm() == null || req.getCruiseSpeedKnots() == null) {
                throw new IllegalArgumentException("distanceKm y cruiseSpeedKnots son requeridos.");
            }
            if (req.getCruiseSpeedKnots() == 0) {
                throw new IllegalArgumentException("cruiseSpeedKnots no puede ser 0.");
            }

            // Construir features (usará avgWind/maxWave del request o llamará a Open-Meteo)
            var f = featureBuilder.build(req);
            float distance     = f.distanceKm();
            float plannedHours = f.plannedHours();
            float avgWind      = f.avgWindKnots();
            float maxWave      = f.maxWaveM();

            // ***** INFERENCIA ONNX *****
            float[] input = new float[] { distance, plannedHours, avgWind, maxWave };
            long[] shape  = new long[] {1, 4};

            double delayHours;
            try (OnnxTensor tensor = OnnxTensor.createTensor(env, java.nio.FloatBuffer.wrap(input), shape)) {
                var result = session.run(java.util.Map.of(inputName, tensor));
                Object val = result.get(outputName).get().getValue();
                if (val instanceof float[])      delayHours = ((float[]) val)[0];
                else if (val instanceof float[][]) delayHours = ((float[][]) val)[0][0];
                else if (val instanceof double[])  delayHours = ((double[]) val)[0];
                else throw new IllegalStateException("Tipo de salida ONNX no soportado: " + val.getClass());
            }

            // ***** PROBABILIDAD/CAUSA (heurística demo) *****
            double prob = Math.max(0, Math.min(1, 0.02 * avgWind + 0.12 * Math.max(0, maxWave - 1)));
            String cause = (maxWave >= 3.5) ? "Oleaje fuerte" : (avgWind >= 25 ? "Viento fuerte" : "Leve");

            // ***** ETA planificado y ajustado *****
            String plannedEtaIso = null, adjustedEtaIso = null;
            if (req.getDepartureTimeIso() != null && !req.getDepartureTimeIso().isBlank()) {
                try {
                    Instant dep = Instant.parse(req.getDepartureTimeIso()); // ISO-8601 Z/offset
                    long plannedSec = Math.round(plannedHours * 3600.0);
                    long totalSec   = Math.round((plannedHours + delayHours) * 3600.0);
                    plannedEtaIso   = dep.plusSeconds(plannedSec).toString();
                    adjustedEtaIso  = dep.plusSeconds(totalSec).toString();
                } catch (DateTimeParseException ignored) {}
            }

            var resp = new WeatherDelayResponse();
            resp.setDelayHours(delayHours);
            resp.setDelayProbability(prob);
            resp.setMainDelayFactor(cause);
            resp.setPlannedEtaIso(plannedEtaIso);
            resp.setAdjustedEtaIso(adjustedEtaIso);
            resp.setUsedFallback(false);

            resp.setUsedAvgWindKnots((double) avgWind);
            resp.setUsedMaxWaveM((double) maxWave);

            log.info("Predicción IA OK en {} ms, delay={} h, plannedEta={}, adjustedEta={}",
                    System.currentTimeMillis()-t0, String.format("%.2f", delayHours), plannedEtaIso, adjustedEtaIso);

            return resp;

        } catch (Exception e) {
            log.error("Error en predicción IA", e);
            var fallback = new WeatherDelayResponse();
            fallback.setDelayHours(0);
            fallback.setDelayProbability(0);
            fallback.setMainDelayFactor("No disponible");
            fallback.setPlannedEtaIso(null);
            fallback.setAdjustedEtaIso(null);
            fallback.setUsedFallback(true);
            fallback.setUsedAvgWindKnots(null);
            fallback.setUsedMaxWaveM(null);
            return fallback;
        }
    }
}
