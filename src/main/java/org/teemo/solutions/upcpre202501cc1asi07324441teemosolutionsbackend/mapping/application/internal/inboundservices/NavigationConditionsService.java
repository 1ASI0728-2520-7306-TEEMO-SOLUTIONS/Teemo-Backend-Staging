package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.application.internal.inboundservices;

import org.springframework.stereotype.Service;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Route;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services.NavigationConditionsProvider;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

@Service
public class NavigationConditionsService implements NavigationConditionsProvider {

    // --- Constantes de Reglas de Negocio ---
    private static final double MONSOON_ADJUSTMENT_FACTOR = 0.2; // 20%
    private static final double ARCTIC_ICE_PENALTY = 1500.0;
    private static final Set<Month> MONSOON_MONTHS = Set.of(Month.JUNE, Month.JULY, Month.AUGUST);
    private static final Set<Month> ARCTIC_SUMMER_MONTHS = Set.of(Month.JUNE, Month.JULY);

    // --- Coordenadas para Zonas Geográficas (Reglas) ---
    private static final double MONSOON_MIN_LON = 65.0;
    private static final double MONSOON_MAX_LON = 100.0;
    private static final double MONSOON_MAX_LAT = 30.0;

    private static final double KUROSHIO_MIN_LON = 130.0;
    private static final double KUROSHIO_MAX_LON = 160.0;
    private static final double KUROSHIO_MIN_LAT = 20.0;
    private static final double KUROSHIO_MAX_LAT = 40.0;

    private static final double GULF_STREAM_MIN_LON = -80.0;
    private static final double GULF_STREAM_MAX_LON = -50.0;
    private static final double GULF_STREAM_MIN_LAT = 25.0;
    private static final double GULF_STREAM_MAX_LAT = 45.0;


    // El servicio ya no necesita PortService. ¡Es independiente!
    public NavigationConditionsService() {}

    @Override
    public double getAdjustedCost(Route route, Port homePort, Port destinationPort, Clock clock) {
        double baseDistance = route.getDistance();

        // El cálculo de ajuste del monzón ahora es más limpio.
        double monsoonAdjustment = calculateMonsoonAdjustment(homePort, baseDistance, clock) +
                calculateMonsoonAdjustment(destinationPort, baseDistance, clock);

        double arcticPenalty = calculateArcticPenalty(route, clock);

        return baseDistance + monsoonAdjustment + arcticPenalty;
    }

    @Override
    public boolean isAlongFavorableCurrent(Port from, Port to) {
        // La lógica es más clara y no accede a la BD
        return isInKuroshioCurrent(from) && isDirectionEast(from, to);
    }

    @Override
    public boolean isAgainstStrongCurrent(Port from, Port to) {
        return isInGulfStreamArea(from) && isDirectionWest(from, to);
    }

    private double calculateMonsoonAdjustment(Port port, double baseDistance, Clock clock) {
        if (isMonsoonSeason(clock) && isInMonsoonZone(port)) {
            return baseDistance * MONSOON_ADJUSTMENT_FACTOR;
        }
        return 0.0;
    }

    private double calculateArcticPenalty(Route route, Clock clock) {
        if (isArcticRoute(route) && !isSummerArctic(clock)) {
            return ARCTIC_ICE_PENALTY;
        }
        return 0.0;
    }

    private boolean isMonsoonSeason(Clock clock) {
        return MONSOON_MONTHS.contains(LocalDate.now(clock).getMonth());
    }

    private boolean isSummerArctic(Clock clock) {
        return ARCTIC_SUMMER_MONTHS.contains(LocalDate.now(clock).getMonth());
    }

    private boolean isInMonsoonZone(Port port) {
        Coordinates coords = port.getCoordinates();
        return coords.longitude() > MONSOON_MIN_LON &&
                coords.longitude() < MONSOON_MAX_LON &&
                coords.latitude() < MONSOON_MAX_LAT;
    }

    private boolean isArcticRoute(Route route) {
        return (route.getHomePort().getContinent().equals("América") && route.getDestinationPort().getContinent().equals("Arkits")) ||
                (route.getHomePort().getContinent().equals("Arkits") && route.getDestinationPort().getContinent().equals("América"));
    }

    private boolean isInKuroshioCurrent(Port port) {
        Coordinates coords = port.getCoordinates();
        return coords.longitude() > KUROSHIO_MIN_LON &&
                coords.longitude() < KUROSHIO_MAX_LON &&
                coords.latitude() > KUROSHIO_MIN_LAT &&
                coords.latitude() < KUROSHIO_MAX_LAT;
    }

    private boolean isInGulfStreamArea(Port port) {
        Coordinates coords = port.getCoordinates();
        return coords.longitude() > GULF_STREAM_MIN_LON &&
                coords.longitude() < GULF_STREAM_MAX_LON &&
                coords.latitude() > GULF_STREAM_MIN_LAT &&
                coords.latitude() < GULF_STREAM_MAX_LAT;
    }

    private boolean isDirectionEast(Port from, Port to) {
        return to.getCoordinates().longitude() > from.getCoordinates().longitude();
    }

    private boolean isDirectionWest(Port from, Port to) {
        return to.getCoordinates().longitude() < from.getCoordinates().longitude();
    }
}