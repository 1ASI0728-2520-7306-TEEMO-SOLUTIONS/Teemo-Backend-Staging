// Port.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities;

import lombok.Getter;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.Objects;

@Getter
public class Port extends AuditableAbstractAggregateRoot<Port> {
    private String name;
    private Coordinates coordinates;
    private String continent;

    public Port(String name, Coordinates coordinates, String continent) {
        this.name = name;
        this.coordinates = coordinates;
        this.continent = continent;
    }

    public Port(String id, String name, Coordinates coordinates, String continent) {
        this();
        this.setId(id);
        this.name = name;
        this.coordinates = coordinates;
        this.continent = continent;
    }

    public Port() {
        super();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Port port)) return false;
        return Objects.equals(name, port.name) &&
                Objects.equals(continent, port.continent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, continent);
    }
}