package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.infrastructure.seeding;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Map;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.aggregates.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreateMapCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.commands.CreatePortCommand;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.domain.model.entities.Edge;

import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.routes.infrastructure.persistence.jpa.repositories.MapRepository;

@Component
@RequiredArgsConstructor
public class MapDataSeeder {

    private final MapRepository mapRepository;

    @PostConstruct
    public void seedData() {
        if (mapRepository.count() > 0) {
            return; // Ya hay datos, no hace falta seedear
        }

        CreateMapCommand createMapCommand = new CreateMapCommand("Mapa Mundial");

        Map worldMap = new Map(createMapCommand);



        // Agregar puertos
        Port newYork = new Port(new CreatePortCommand("New York", 55.7128, -106.0060, "América"));
        Port tokyo = new Port(new CreatePortCommand("Tokyo", 45.6895, -58.6917, "Asia"));
        Port panama = new Port(new CreatePortCommand("Panama City", 20.9824, -109.5199, "América"));

        worldMap.addPort(newYork);
        worldMap.addPort(tokyo);
        worldMap.addPort(panama);

        // Agregar rutas (edges)
        Edge nyToPanama = new Edge(null, newYork, panama, 1000.0, 0.4, "in");
        Edge panamaToTokyo = new Edge(null, panama, tokyo, 5000.0, 0.2, "out");

        worldMap.addEdge(nyToPanama);
        worldMap.addEdge(panamaToTokyo);

        mapRepository.save(worldMap);

        System.out.println("✅ Datos de puertos y rutas iniciales cargados exitosamente.");
    }
}
