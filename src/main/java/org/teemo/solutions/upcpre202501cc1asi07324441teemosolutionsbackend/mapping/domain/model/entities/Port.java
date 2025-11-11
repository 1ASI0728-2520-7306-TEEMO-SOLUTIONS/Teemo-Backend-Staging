// Port.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities;

import lombok.Getter;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.Instant;
import java.util.Objects;

@Getter
public class Port extends AuditableAbstractAggregateRoot<Port> {
    private String name;
    private Coordinates coordinates;
    private String continent;
    private boolean disabled;
    private String disabledReason;
    private Instant disabledAt;
    private String disabledBy;

    public Port(String name, Coordinates coordinates, String continent) {
        this();
        this.name = name;
        this.coordinates = coordinates;
        this.continent = continent;
    }

    public Port(String id, String name, Coordinates coordinates, String continent) {
        this(name, coordinates, continent);
        this.setId(id);
    }

    public Port(String id, String name, Coordinates coordinates, String continent,
                boolean disabled, String disabledReason, Instant disabledAt, String disabledBy) {
        this(id, name, coordinates, continent);
        this.disabled = disabled;
        this.disabledReason = disabledReason;
        this.disabledAt = disabledAt;
        this.disabledBy = disabledBy;
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

    public void updateDisabledState(boolean disabled, String reason, Instant timestamp, String actor) {
        this.disabled = disabled;
        this.disabledReason = reason;
        this.disabledAt = timestamp;
        this.disabledBy = actor;
    }
}
