package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Propiedades para la integración con el API público de alertas NOAA (weather.gov).
 */
@ConfigurationProperties(prefix = "ai.weather.hazard.noaa")
public class NoaaAlertsProperties {

    private boolean enabled = true;
    private String baseUrl = "https://api.weather.gov";
    private Duration timeout = Duration.ofSeconds(6);
    private double latPaddingDeg = 4.0;
    private double lonPaddingDeg = 4.0;
    private int maxResults = 25;
    private double defaultRadiusKm = 550.0;
    private String userAgent = "TeemoHazardService/1.0 (+support@teemo.solutions)";
    private List<String> cycloneEvents = new ArrayList<>(List.of(
            "Hurricane Warning",
            "Hurricane Watch",
            "Tropical Storm Warning",
            "Tropical Storm Watch",
            "Typhoon Warning",
            "Typhoon Watch"
    ));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        if (timeout != null) {
            this.timeout = timeout;
        }
    }

    public double getLatPaddingDeg() {
        return latPaddingDeg;
    }

    public void setLatPaddingDeg(double latPaddingDeg) {
        this.latPaddingDeg = latPaddingDeg;
    }

    public double getLonPaddingDeg() {
        return lonPaddingDeg;
    }

    public void setLonPaddingDeg(double lonPaddingDeg) {
        this.lonPaddingDeg = lonPaddingDeg;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public double getDefaultRadiusKm() {
        return defaultRadiusKm;
    }

    public void setDefaultRadiusKm(double defaultRadiusKm) {
        this.defaultRadiusKm = defaultRadiusKm;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public List<String> getCycloneEvents() {
        return cycloneEvents;
    }

    public void setCycloneEvents(List<String> cycloneEvents) {
        if (cycloneEvents == null || cycloneEvents.isEmpty()) {
            return;
        }
        this.cycloneEvents = new ArrayList<>(cycloneEvents);
    }
}
