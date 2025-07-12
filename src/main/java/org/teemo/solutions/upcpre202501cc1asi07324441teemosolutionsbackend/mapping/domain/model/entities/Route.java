package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities;

import lombok.Getter;
@Getter
public class Route {
    private final Port homePort;
    private final Port destinationPort;
    private final Double distance;


    public Route(Port homePort, Port destinationPort, Double distance) {
        this.homePort = homePort;
        this.destinationPort = destinationPort;
        this.distance = distance;
    }
}