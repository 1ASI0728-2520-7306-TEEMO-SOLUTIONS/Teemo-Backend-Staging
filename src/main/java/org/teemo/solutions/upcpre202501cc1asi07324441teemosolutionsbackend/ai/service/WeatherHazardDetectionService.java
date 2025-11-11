package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.service;

import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto.WeatherHazardProbability;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Heurística interna y libre de dependencias externas.
 * - HURRICANE: franja tropical (lat 5..30) y meses pico Atlántico (Ago-Oct) y Pacífico (Jul-Oct)
 * - ICE: lat >= 65 en meses fríos (Dic-Mar)
 * - MAREAJE (swell): lat -40..-10 en Pacífico Sur; año redondo pero menor prob.
 *
 * NOTA: no altera delayHours, sólo llena probabilidades y routeViable.
 */
@Service
public class WeatherHazardDetectionService {

    public static class HazardEval {
        public boolean routeViable = true;
        public String nonViableReason = null;
        public double overallHazardProbability = 0.0;
        public List<WeatherHazardProbability> hazards = new ArrayList<>();
    }

    public HazardEval evaluate(
            double originLat, double originLon,
            double destLat, double destLon,
            Instant departure
    ) {
        int month = departure.atZone(ZoneOffset.UTC).getMonthValue();

        List<WeatherHazardProbability> out = new ArrayList<>();
        double maxProb = 0.0;
        boolean viable = true;
        String reason = null;

        // ---- ICE (ej. Ártico) ----
        double iceProb = iceProbabilityAlongRoute(originLat, destLat, month);
        if (iceProb > 0.0) {
            WeatherHazardProbability h = new WeatherHazardProbability();
            h.setType("ICE");
            h.setProbability(iceProb);
            h.setZoneName("Zonas árticas / alto latitud");
            // Centro aproximado (heurística simple)
            h.setLatCenter((originLat + destLat) / 2.0);
            h.setLonCenter((originLon + destLon) / 2.0);
            h.setRadiusKm(400.0);
            h.setMonth(month);
            out.add(h);
            maxProb = Math.max(maxProb, iceProb);

            // Si hay hielo con prob. alta en temporada fría, marcamos no viable
            if (iceProb >= 0.6 && isColdSeason(month)) {
                viable = false;
                reason = "Hielo estacional probable en la ruta";
            }
        }

        // ---- HURRICANE (Atlántico/Caribe/Pacífico tropical) ----
        double hurProb = hurricaneProbability(originLat, originLon, destLat, destLon, month);
        if (hurProb > 0.0) {
            WeatherHazardProbability h = new WeatherHazardProbability();
            h.setType("HURRICANE");
            h.setProbability(hurProb);
            h.setZoneName("Franja tropical propensa a ciclones");
            h.setLatCenter((originLat + destLat) / 2.0);
            h.setLonCenter((originLon + destLon) / 2.0);
            h.setRadiusKm(600.0);
            h.setMonth(month);
            out.add(h);
            maxProb = Math.max(maxProb, hurProb);
        }

        // ---- MAREAJE (swell) Pacífico Sur (simple) ----
        double swellProb = swellProbability(originLat, originLon, destLat, destLon);
        if (swellProb > 0.0) {
            WeatherHazardProbability h = new WeatherHazardProbability();
            h.setType("MAREAJE");
            h.setProbability(swellProb);
            h.setZoneName("Pacífico Sur (swell)");
            h.setLatCenter(-25.0);
            h.setLonCenter(-120.0);
            h.setRadiusKm(800.0);
            h.setMonth(month);
            out.add(h);
            maxProb = Math.max(maxProb, swellProb);
        }

        HazardEval eval = new HazardEval();
        eval.routeViable = viable;
        eval.nonViableReason = reason;
        // Índice compuesto sencillo = máx(probabilidades)
        eval.overallHazardProbability = clamp01(maxProb);
        eval.hazards = out;
        return eval;
    }

    private static boolean isColdSeason(int month) {
        // Dic(12), Ene(1), Feb(2), Mar(3)
        return month == 12 || month <= 3;
    }

    private static double iceProbabilityAlongRoute(double latA, double latB, int month) {
        // Si cualquiera de los extremos cruza >= 65° lat en temporada fría, asigna prob.
        double maxLat = Math.max(Math.abs(latA), Math.abs(latB));
        if (maxLat >= 75 && isColdSeason(month)) return 0.8;
        if (maxLat >= 70 && isColdSeason(month)) return 0.6;
        if (maxLat >= 65 && isColdSeason(month)) return 0.4;
        return 0.0;
    }

    private static double hurricaneProbability(double latA, double lonA, double latB, double lonB, int month) {
        // Franja tropical 5..30° | picos: Atlántico (Ago-Oct), Pacífico (Jul-Oct)
        boolean crossesTropics =
                (inRangeAbs(latA, 5, 30) || inRangeAbs(latB, 5, 30));
        if (!crossesTropics) return 0.0;

        boolean atlSeason = (month >= 8 && month <= 10);
        boolean pacSeason = (month >= 7 && month <= 10);

        double base = 0.25; // base si transita franja tropical
        if (atlSeason || pacSeason) base += 0.25; // pico estacional
        // Si la ruta pasa por longitudes comunes en Atlántico tropical (-100..-20)
        boolean inAtlanticLong = (inRange(lonA, -100, -20) || inRange(lonB, -100, -20));
        if (inAtlanticLong && atlSeason) base += 0.2;

        return clamp01(base);
    }

    private static double swellProbability(double latA, double lonA, double latB, double lonB) {
        // Pacífico Sur aprox: lat -40..-10 y lon -160..-70
        boolean inLat = (inRange(latA, -40, -10) || inRange(latB, -40, -10));
        boolean inLon = (inRange(lonA, -160, -70) || inRange(lonB, -160, -70));
        if (inLat && inLon) return 0.35;
        return 0.0;
    }

    private static boolean inRange(double v, double a, double b) {
        return v >= a && v <= b;
    }
    private static boolean inRangeAbs(double v, double a, double b) {
        double av = Math.abs(v);
        return av >= a && av <= b;
    }
    private static double clamp01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }
}
