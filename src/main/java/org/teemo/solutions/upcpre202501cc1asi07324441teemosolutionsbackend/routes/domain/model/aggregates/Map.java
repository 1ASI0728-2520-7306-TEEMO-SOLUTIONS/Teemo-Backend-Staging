package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreateMapCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.entities.Edge;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Map extends AuditableAbstractAggregateRoot<Map> {

    @NotBlank
    @Column(unique = true)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "map_id")
    private List<Port> ports = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "map_id")
    private List<Edge> edges = new ArrayList<>();

    public Map(CreateMapCommand command){
        this.name = command.name();
        this.ports = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public void addPort(Port port) {
        ports.add(port);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}
