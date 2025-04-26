package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreatePortCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Port extends AuditableAbstractAggregateRoot<Port> {

    @NotBlank
    @Column(unique = true)
    private String name;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotBlank
    private String continent;

    // Constructor desde comando
    public Port(CreatePortCommand command) {
        this.name = command.name();
        this.latitude = command.latitude();
        this.longitude = command.longitude();
        this.continent = command.continent();
    }
}
