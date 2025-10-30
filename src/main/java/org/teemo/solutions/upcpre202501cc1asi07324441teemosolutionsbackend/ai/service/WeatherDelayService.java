package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.service;

import ai.onnxruntime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.WeatherDelayRequest;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.WeatherDelayResponse;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Service
public class WeatherDelayService {
    private static final Logger log = LoggerFactory.getLogger(WeatherDelayService.class);

    private final OrtEnvironment env;
    private final OrtSession session;
    private final String inputName;
    private final String outputName;

    private final WeatherFeatureBuilder featureBuilder;

    // ====== Parámetros de realismo (ajústalos si lo ves necesario) ======
    /** Fracción del tiempo planificado que como máximo aporta el score del modelo. */
    private static final double SENSITIVITY = 0.10;         // 10%
    /** Tope por fracción del tiempo planificado. */
    private static final double MAX_FRACTION = 0.25;        // 25%
    /** Tope absoluto de retraso en horas. */
    private static final double ABSOLUTE_CAP_HOURS = 72.0;  // 72 h (3 días)

    public WeatherDelayService(OrtEnvironment env, OrtSession session, WeatherFeatureBuilder featureBuilder) throws OrtException {
        this.env = env;
        this.session = session;
        this.featureBuilder = featureBuilder;

        // Descubrir nombres reales de entradas/salidas y registrarlos
        this.inputName = session.getInputInfo().keySet().iterator().next();
        this.outputName = session.getOutputInfo().keySet().iterator().next();
        log.info("[ONNX] inputName='{}', outputName='{}'", inputName, outputName);

        session.getInputInfo().forEach((k, v) -> log.info("[ONNX] Available input -> {}", k));
        session.getOutputInfo().forEach((k, v) -> log.info("[ONNX] Available output -> {}", k));
    }

    public WeatherDelayResponse predict(WeatherDelayRequest req) {
        long t0 = System.currentTimeMillis();
        try {
            // Validación mínima
            if (req.getCruiseSpeedKnots() == null || req.getCruiseSpeedKnots() == 0) {
                throw new IllegalArgumentException("cruiseSpeedKnots es requerido y debe ser > 0.");
            }
            // Si no viene distanceKm, intentamos calcularla con lat/lon (tramo)
            if (req.getDistanceKm() == null) {
                if (req.getOriginLat() != null && req.getOriginLon() != null
                        && req.getDestLat() != null && req.getDestLon() != null) {
                    double dKm = haversineKm(req.getOriginLat(), req.getOriginLon(), req.getDestLat(), req.getDestLon());
                    req.setDistanceKm(dKm);
                } else {
                    throw new IllegalArgumentException("distanceKm es requerido si no se proporcionan coordenadas de origen y destino.");
                }
            }

            // Construir features (si no vienen avgWind/maxWave, el builder los completa)
            var f = featureBuilder.build(req);
            float distanceKm   = f.distanceKm();
            float plannedHours = f.plannedHours();
            float avgWindKn    = f.avgWindKnots();
            float maxWaveM     = f.maxWaveM();

            // Si llegaron coordenadas, volvemos a priorizar la distancia del TRAMO por seguridad
            if (req.getOriginLat() != null && req.getOriginLon() != null
                    && req.getDestLat() != null && req.getDestLon() != null) {
                double dKm = haversineKm(req.getOriginLat(), req.getOriginLon(), req.getDestLat(), req.getDestLon());
                if (dKm > 1.0) { // distancia válida
                    distanceKm = (float) dKm;
                    // knots -> km/h: 1 kn = 1.852 km/h
                    double speedKmH = req.getCruiseSpeedKnots() * 1.852;
                    plannedHours = (float) (distanceKm / speedKmH);
                }
            }

            // ---------------- INFERENCIA ONNX ----------------
            // Orden de features alineado con el entrenamiento:
            // distanceKm, avgWindKnots, maxWaveM, plannedHours
            float[] input = new float[] { distanceKm, avgWindKn, maxWaveM, plannedHours };
            long[] shape  = new long[] { 1, 4 };

            double modelScore;
            try (OnnxTensor tensor = OnnxTensor.createTensor(
                    env, java.nio.FloatBuffer.wrap(input), shape)) {
                try (OrtSession.Result result = session.run(Map.of(inputName, tensor))) {
                    Object val = result.get(outputName).get().getValue();
                    if (val instanceof float[])        modelScore = ((float[])   val)[0];
                    else if (val instanceof float[][]) modelScore = ((float[][]) val)[0][0];
                    else if (val instanceof double[])  modelScore = ((double[])  val)[0];
                    else throw new IllegalStateException("Tipo de salida ONNX no soportado: " + val.getClass());
                }
            }

            // Interpretamos la salida del modelo como SCORE [0..1]
            double scoreClamped = Math.max(0.0, Math.min(1.0, modelScore));
            double rawDelay     = plannedHours * scoreClamped * SENSITIVITY;

            // Topes de realismo
            double realisticCap = Math.min(plannedHours * MAX_FRACTION, ABSOLUTE_CAP_HOURS);
            double delayHours   = Math.min(Math.max(0.0, rawDelay), realisticCap);

            // ---------------- PROBABILIDAD/CAUSA (heurística ligera) ----------------
            double prob = Math.min(1.0, Math.max(0.0,
                    0.02 * avgWindKn + 0.12 * Math.max(0.0, maxWaveM - 1.0)));
            String cause = (maxWaveM >= 3.5)
                    ? "Oleaje fuerte"
                    : (avgWindKn >= 25.0 ? "Viento fuerte" : "Condiciones leves");

            // ---------------- ETA planificado y ajustado ----------------
            String plannedEtaIso = null, adjustedEtaIso = null;
            if (req.getDepartureTimeIso() != null && !req.getDepartureTimeIso().isBlank()) {
                try {
                    Instant dep = Instant.parse(req.getDepartureTimeIso()); // ISO-8601 (Z/offset)
                    long plannedSec = Math.round(plannedHours * 3600.0);
                    long totalSec   = Math.round((plannedHours + delayHours) * 3600.0);
                    plannedEtaIso   = dep.plusSeconds(plannedSec).toString();
                    adjustedEtaIso  = dep.plusSeconds(totalSec).toString();
                } catch (DateTimeParseException ex) {
                    log.warn("departureTimeIso no es ISO-8601 válido: '{}'", req.getDepartureTimeIso());
                }
            }

            // ---------------- Logging de trazabilidad ----------------
            log.info("[IA] features => distanceKm={}, plannedHours={}, avgWindKnots={}, maxWaveM={}",
                    String.format("%.2f", distanceKm),
                    String.format("%.2f", plannedHours),
                    String.format("%.2f", avgWindKn),
                    String.format("%.2f", maxWaveM));
            log.info("[IA] modelScore={} (clamped={}) rawDelay={}h cap={}h => delay={}h",
                    String.format("%.4f", modelScore),
                    String.format("%.4f", scoreClamped),
                    String.format("%.2f", rawDelay),
                    String.format("%.2f", realisticCap),
                    String.format("%.2f", delayHours));

            // ---------------- Respuesta ----------------
            var resp = new WeatherDelayResponse();
            resp.setDelayHours(delayHours);
            resp.setDelayProbability(prob);
            resp.setMainDelayFactor(cause);
            resp.setPlannedEtaIso(plannedEtaIso);
            resp.setAdjustedEtaIso(adjustedEtaIso);
            resp.setUsedFallback(false);

            resp.setUsedAvgWindKnots((double) avgWindKn);
            resp.setUsedMaxWaveM((double) maxWaveM);

            log.info("Predicción IA OK en {} ms, delay={} h, plannedEta={}, adjustedEta={}",
                    System.currentTimeMillis() - t0,
                    String.format("%.2f", delayHours),
                    plannedEtaIso, adjustedEtaIso);

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

    // ====== Util: distancia geodésica para tramo ======
    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0088; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}
