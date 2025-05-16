package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices;

import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.RouteEdge;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;

import java.time.Month;
import java.time.LocalDate;

@Service
public class NavigationConditionsService {

    private static final double MONSOON_ADJUSTMENT = 0.2;
    private static final double ARCTIC_ICE_PENALTY = 1500;

    public double applyEnvironmentalFactors(RouteEdge edge, Port currentPort) {
        double baseDistance = edge.getDistance();
        return baseDistance +
                calculateMonsoonAdjustment(currentPort, baseDistance) +
                calculateArcticPenalty(edge);
    }

    private double calculateMonsoonAdjustment(Port port, double baseDistance) {
        return isMonsoonSeason() && isInMonsoonZone(port) ?
                baseDistance * MONSOON_ADJUSTMENT : 0;
    }

    private double calculateArcticPenalty(RouteEdge edge) {
        return isArcticRoute(edge) && !isSummerArctic() ? ARCTIC_ICE_PENALTY : 0;
    }

    private boolean isMonsoonSeason() {
        Month currentMonth = LocalDate.now().getMonth();
        return currentMonth == Month.JUNE || currentMonth == Month.JULY || currentMonth == Month.AUGUST;
    }

    private boolean isInMonsoonZone(Port port) {
        return isInIndianOceanRegion(port.getCoordinates());
    }

    // Métodos auxiliares para regiones específicas
    private boolean isInIndianOceanRegion(Coordinates coords) {
        return coords.longitude() > 65 &&
                coords.longitude() < 100 &&
                coords.latitude() < 30;
    }

    private boolean isArcticRoute(RouteEdge edge) {
        return edge.getDestination().getContinent().equals("América") &&
                edge.getDestination().getName().equals("Arkits");
    }

    private boolean isSummerArctic() {
        Month currentMonth = LocalDate.now().getMonth();
        return currentMonth == Month.JUNE || currentMonth == Month.JULY;
    }

    public boolean isAlongFavorableCurrent(Port a, Port b) {
        return isInKuroshioCurrent(a) && isDirectionEast(a, b);
    }

    public boolean isAgainstStrongCurrent(Port a, Port b) {
        return isInGulfStreamArea(a) && isDirectionWest(a, b);
    }

    private boolean isInKuroshioCurrent(Port port) {
        return port.getCoordinates().longitude() > 130 &&
                port.getCoordinates().longitude() < 160 &&
                port.getCoordinates().latitude() > 20 &&
                port.getCoordinates().latitude() < 40;
    }

    private boolean isInGulfStreamArea(Port port) {
        return port.getCoordinates().longitude() > -80 &&
                port.getCoordinates().longitude() < -50 &&
                port.getCoordinates().latitude() > 25 &&
                port.getCoordinates().latitude() < 45;
    }

    private boolean isDirectionEast(Port a, Port b) {
        return b.getCoordinates().longitude() > a.getCoordinates().longitude();
    }

    private boolean isDirectionWest(Port a, Port b) {
        return b.getCoordinates().longitude() < a.getCoordinates().longitude();
    }
}