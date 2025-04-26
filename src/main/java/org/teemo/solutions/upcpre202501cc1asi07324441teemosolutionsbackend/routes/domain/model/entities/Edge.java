package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Port;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Edge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "origin_port_id", nullable = false)
    private Port originPort;

    @ManyToOne
    @JoinColumn(name = "destination_port_id", nullable = false)
    private Port destinationPort;

    @NotNull
    private Double distance;

    @NotNull
    private Double curvature;

    @NotBlank
    private String direction;
}
