// GeoUtils.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects;

import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
@Component // <-- Añadir esta anotación

public class GeoUtils {
    private static final double EARTH_RADIUS_KM = 6371;

    public double calculateHaversineDistance(Port a, Port b) {
        return calculateHaversineDistance(a.getCoordinates(), b.getCoordinates());
    }

    public double calculateHaversineDistance(Coordinates coord1, Coordinates coord2) {
        double lat1 = Math.toRadians(coord1.latitude());
        double lon1 = Math.toRadians(coord1.longitude());
        double lat2 = Math.toRadians(coord2.latitude());
        double lon2 = Math.toRadians(coord2.longitude   ());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(dLon / 2), 2);

        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}