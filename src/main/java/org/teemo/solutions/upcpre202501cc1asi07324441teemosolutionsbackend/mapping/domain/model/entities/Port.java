// Port.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.Objects;

@Getter
@Setter
@Document(collection = "ports")
@CompoundIndex(name = "unique_name_continent", def = "{'name': 1, 'continent': 1}", unique = true)
public class Port extends AuditableAbstractAggregateRoot<Port> {
    private final String name;
    private final Coordinates coordinates;
    private final String continent;
    private Integer maxShipSize; // En TEUs
    private Double draft; // Calado máximo en metros

    public Port(String name, Coordinates coordinates, String continent) {
        this.name = name;
        this.coordinates = coordinates;
        this.continent = continent;
    }

    public Port(String name, Coordinates coordinates, String continent,
                Integer maxShipSize, Double draft) {
        this(name, coordinates, continent);
        this.maxShipSize = maxShipSize;
        this.draft = draft;
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