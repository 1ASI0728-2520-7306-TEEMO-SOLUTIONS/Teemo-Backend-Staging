package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.dto;

import java.util.List;

public class HazardAssessmentResponse {

    public static class Segment {
        public double startLat;
        public double startLon;
        public double endLat;
        public double endLon;

        public List<Hazard> hazards;

        public boolean notViableDueToIce;
        public String  advisory;

        // getters/setters
        public double getStartLat() { return startLat; }
        public void setStartLat(double startLat) { this.startLat = startLat; }
        public double getStartLon() { return startLon; }
        public void setStartLon(double startLon) { this.startLon = startLon; }
        public double getEndLat() { return endLat; }
        public void setEndLat(double endLat) { this.endLat = endLat; }
        public double getEndLon() { return endLon; }
        public void setEndLon(double endLon) { this.endLon = endLon; }
        public List<Hazard> getHazards() { return hazards; }
        public void setHazards(List<Hazard> hazards) { this.hazards = hazards; }
        public boolean isNotViableDueToIce() { return notViableDueToIce; }
        public void setNotViableDueToIce(boolean notViableDueToIce) { this.notViableDueToIce = notViableDueToIce; }
        public String getAdvisory() { return advisory; }
        public void setAdvisory(String advisory) { this.advisory = advisory; }
    }

    public static class Hazard {
        public String type;        // "HURRICANE","ICE","LARGE_SWELL", etc.
        public double probability; // 0..1
        public String severity;    // "LOW","MEDIUM","HIGH"
        public String windowStartIso;
        public String windowEndIso;
        public String regionName;
        public String source;
        public String rationale;

        // NUEVO: ubicación opcional del fenómeno para dibujar en el mapa
        public Double latCenter;   // nullable
        public Double lonCenter;   // nullable
        public Double radiusKm;    // nullable

        // getters/setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public double getProbability() { return probability; }
        public void setProbability(double probability) { this.probability = probability; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getWindowStartIso() { return windowStartIso; }
        public void setWindowStartIso(String windowStartIso) { this.windowStartIso = windowStartIso; }
        public String getWindowEndIso() { return windowEndIso; }
        public void setWindowEndIso(String windowEndIso) { this.windowEndIso = windowEndIso; }
        public String getRegionName() { return regionName; }
        public void setRegionName(String regionName) { this.regionName = regionName; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }

        public Double getLatCenter() { return latCenter; }
        public void setLatCenter(Double latCenter) { this.latCenter = latCenter; }
        public Double getLonCenter() { return lonCenter; }
        public void setLonCenter(Double lonCenter) { this.lonCenter = lonCenter; }
        public Double getRadiusKm() { return radiusKm; }
        public void setRadiusKm(Double radiusKm) { this.radiusKm = radiusKm; }
    }

    public String season;
    public int    month;
    public String hemisphere;
    public double routeDistanceKm;
    public double plannedHours;
    public List<Segment> segments;

    // getters/setters
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public String getHemisphere() { return hemisphere; }
    public void setHemisphere(String hemisphere) { this.hemisphere = hemisphere; }
    public double getRouteDistanceKm() { return routeDistanceKm; }
    public void setRouteDistanceKm(double routeDistanceKm) { this.routeDistanceKm = routeDistanceKm; }
    public double getPlannedHours() { return plannedHours; }
    public void setPlannedHours(double plannedHours) { this.plannedHours = plannedHours; }
    public List<Segment> getSegments() { return segments; }
    public void setSegments(List<Segment> segments) { this.segments = segments; }
}
