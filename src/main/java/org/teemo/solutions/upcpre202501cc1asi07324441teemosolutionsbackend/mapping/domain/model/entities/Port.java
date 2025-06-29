// Port.java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities;

import lombok.Getter;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.Objects;

@Getter
// Elimina @Document(collection = "ports")
public class Port extends AuditableAbstractAggregateRoot<Port> {
    private String name;
    private Coordinates coordinates;
    private String continent;

    // El constructor con @PersistenceConstructor ya no es necesario para el dominio.
    // Podemos tener un único constructor que asegure la consistencia.
    public Port(String name, Coordinates coordinates, String continent) {
        this.name = name;
        this.coordinates = coordinates;
        this.continent = continent;
    }

    // Este constructor vacío es para frameworks, pero si el dominio es puro,
    // podríamos incluso eliminarlo si no lo necesita ningún otro componente del dominio.
    // Por ahora, lo mantenemos por si algún serializador lo requiere.
    public Port() {}

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