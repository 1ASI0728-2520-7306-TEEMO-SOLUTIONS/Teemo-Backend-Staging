package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.services;


import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Port;

import java.util.List;

public interface MapGraphService {
    List<String> findOptimalRoute(String from, String to);
    List<Port> getAllPorts();
    Port getPortByName(String name);
}
