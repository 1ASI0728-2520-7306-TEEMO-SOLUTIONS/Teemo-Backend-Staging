package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Route;
import java.time.Clock;

public interface NavigationConditionsProvider {
    /**
     * Calcula el coste ajustado de una ruta considerando factores ambientales.
     * @param route La ruta con su distancia base.
     * @param homePort El objeto Port de origen.
     * @param destinationPort El objeto Port de destino.
     * @param clock El reloj para obtener la fecha actual (permite testing).
     * @return El coste total ajustado.
     */
    double getAdjustedCost(Route route, Port homePort, Port destinationPort, Clock clock);

    /**
     * Verifica si la dirección de viaje es a favor de una corriente favorable.
     * @param from El puerto de origen.
     * @param to El puerto de destino.
     * @return true si la ruta se beneficia de la corriente.
     */
    boolean isAlongFavorableCurrent(Port from, Port to);

    /**
     * Verifica si la dirección de viaje es en contra de una corriente fuerte.
     * @param from El puerto de origen.
     * @param to El puerto de destino.
     * @return true si la ruta es penalizada por la corriente.
     */
    boolean isAgainstStrongCurrent(Port from, Port to);
}