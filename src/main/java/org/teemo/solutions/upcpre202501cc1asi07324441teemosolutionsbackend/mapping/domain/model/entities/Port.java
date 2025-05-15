package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

@Getter
@Setter
@AllArgsConstructor
public class Port extends AuditableAbstractAggregateRoot<Port> {
    private String name;
    private Coordinates coordinates;
    private String continent;
}