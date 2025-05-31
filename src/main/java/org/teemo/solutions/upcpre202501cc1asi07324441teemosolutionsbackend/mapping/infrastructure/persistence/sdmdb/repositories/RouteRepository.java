// RouteRepository.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.repositories;


import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.domain.RouteDocument;

import java.util.List;

public interface RouteRepository{

    RouteDocument getBetweenPorts(String homePort, String destinationPort);
    void saveAll(List<RouteDocument> routes);
    List<RouteDocument> getAll();

    boolean existsByHomePortAndDestinationPort(
            String homePort, String destinationPort);
}