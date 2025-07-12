package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.entities.Port;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.domain.model.valueobjects.Coordinates;

@Document(collection = "ports")
@Getter
@NoArgsConstructor
@AllArgsConstructor
// --- INICIO DEL CÓDIGO AÑADIDO/MODIFICADO ---
@CompoundIndex(name = "name_continent_unique_idx", def = "{'name': 1, 'continent': 1}", unique = true)
// --- FIN DEL CÓDIGO AÑADIDO/MODIFICADO ---
public class PortDocument {
    @Id
    private String id;
    private String name;
    private CoordinatesDocument coordinates;
    private String continent;

    @Data
    public static class CoordinatesDocument {
        private double latitude;
        private double longitude;

        public CoordinatesDocument(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public PortDocument(String name, CoordinatesDocument coordinates, String continent) {
        this.name = name;
        this.coordinates = coordinates;
        this.continent = continent;
    }

    public Port toDomain() {
        return new Port(
            this.name,
            new Coordinates(this.coordinates.latitude, this.coordinates.longitude),
            this.continent
        );
    }
}