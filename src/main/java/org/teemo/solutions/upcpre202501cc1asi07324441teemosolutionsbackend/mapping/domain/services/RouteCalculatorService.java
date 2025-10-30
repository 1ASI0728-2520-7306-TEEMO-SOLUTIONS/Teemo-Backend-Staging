package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.services;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;

import java.util.List;

public interface    RouteCalculatorService {
    List<Port> calculateOptimalRoute(Port start, Port end);
}