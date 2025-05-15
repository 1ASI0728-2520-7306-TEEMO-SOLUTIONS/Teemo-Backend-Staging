package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.aggregates;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;

import java.util.List;
import java.util.Map;

// SafetyValidator.java
@Component
public class SafetyValidator {

    private static final Map<String, RiskLevel> PORT_RISKS = Map.of(
            "Alexandria", RiskLevel.HIGH,
            "Toamasina", RiskLevel.HIGH,
            "Eupatoria", RiskLevel.HIGH,
            "Dubai", RiskLevel.MEDIUM,
            "Shanghai", RiskLevel.MEDIUM
    );

    public List<String> validateRoute(List<Port> route) {
        return route.stream()
                .filter(port -> PORT_RISKS.containsKey(port.getName()))
                .map(port -> formatWarning(port, PORT_RISKS.get(port.getName())))
                .toList();
    }

    private String formatWarning(Port port, RiskLevel risk) {
        return String.format("[%s] %s: %s",
                risk.name(),
                port.getName(),
                risk.getDescription()
        );
    }

    @Getter
    private enum RiskLevel {
        HIGH("Zona de alto riesgo - Evitar si es posible"),
        MEDIUM("Zona de riesgo moderado - Tomar precauciones");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

    }
}