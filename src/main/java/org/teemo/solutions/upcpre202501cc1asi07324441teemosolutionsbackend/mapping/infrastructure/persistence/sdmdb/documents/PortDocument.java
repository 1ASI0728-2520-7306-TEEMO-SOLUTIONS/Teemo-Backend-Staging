package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.mapping.infrastructure.persistence.sdmdb.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data // @Data incluye @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Document(collection = "ports")
public class PortDocument {
    @Id
    private String id;
    private String name;
    private CoordinatesDocument coordinates;
    private String continent;
    private Date createdAt;
    private Date updatedAt;

    // Sub-documento para las coordenadas
    @Data
    public static class CoordinatesDocument {
        private double latitude;
        private double longitude;
    }
}